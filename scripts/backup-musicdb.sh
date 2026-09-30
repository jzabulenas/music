#!/usr/bin/env bash
#
# backup-musicdb.sh — manual backup of the MusicDiscover MariaDB database.
#
# The "music" database is the app's only durable state (users, likes,
# recommendations). This script dumps it into /var/backups/music as a
# gzipped SQL file, verifies the dump, and prunes old backups so only
# the newest KEEP remain.
#
# Usage:
#   sudo ./backup-musicdb.sh
#
# Restore (into an existing MariaDB instance, as root):
#   gunzip < /var/backups/music/music-backup-<timestamp>.sql.gz | mariadb music
#
#   The dump includes flyway_schema_history, so the backend boots cleanly
#   against a restored database without re-running migrations. The Spring
#   Session tables ride along too; they are harmless and simply expire.
#
# Authentication:
#   Debian's MariaDB root user authenticates via the unix_socket plugin, so a
#   root-run script needs no password. If your server's root user requires a
#   password, create a credentials file:
#
#       # /etc/mysql/backup.cnf    (chmod 600, owned by root)
#       [client]
#       user=root
#       password="..."
#
#   and run: sudo EXTRA_DEFAULTS=/etc/mysql/backup.cnf ./backup-musicdb.sh
#
# Automation later: exit code 0 plus timestamped stdout make this safe to wrap
# in a systemd timer or cron job without changes.
#
# Exit codes:
#   0  backup created and verified
#   1  bad environment (not root, missing tools, unreachable database)
#   2  dump or verification failed (no backup written)

# Strict mode: -e aborts on any command failure, -u errors on unset variables
# (catches typos before they can misfire), and pipefail makes a pipeline fail
# if any stage fails — so a mariadb-dump that dies mid-stream is detected
# even though gzip itself succeeds on the partial input.
set -euo pipefail

# ---------------------------------------------------------------------------
# Configuration (override via environment if needed)
# ---------------------------------------------------------------------------
BACKUP_DIR="${BACKUP_DIR:-/var/backups/music}"  # where dumps are stored
DATABASE="${DATABASE:-music}"                   # database to dump
KEEP="${KEEP:-30}"                              # how many dumps to retain
EXTRA_DEFAULTS="${EXTRA_DEFAULTS:-}"            # optional MariaDB credentials file

MARIADB_DUMP="${MARIADB_DUMP:-mariadb-dump}"
MARIADB="${MARIADB:-mariadb}"

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
log() { printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"; }
die() {
    log "ERROR: $1" >&2
    exit "${2:-1}"
}

# ---------------------------------------------------------------------------
# Preflight
# ---------------------------------------------------------------------------
if [[ -z "$EXTRA_DEFAULTS" && $(id -u) -ne 0 ]]; then
    die "must run as root (MariaDB root uses unix_socket auth and $BACKUP_DIR is root-owned); or provide EXTRA_DEFAULTS with explicit credentials"
fi
if [[ -n "$EXTRA_DEFAULTS" && $(id -u) -ne 0 ]]; then
    log "WARNING: running without root; relying on $EXTRA_DEFAULTS for access"
fi

command -v "$MARIADB_DUMP" >/dev/null 2>&1 || die "mariadb-dump not found (install with: apt install mariadb-client)"
[[ "$KEEP" =~ ^[0-9]+$ ]] && (( KEEP >= 1 )) || die "KEEP must be a positive integer, got: $KEEP"

client_args=()
if [[ -n "$EXTRA_DEFAULTS" ]]; then
    [[ -r "$EXTRA_DEFAULTS" ]] || die "EXTRA_DEFAULTS is not readable: $EXTRA_DEFAULTS"
    # --defaults-extra-file must be the first option to take effect.
    client_args=(--defaults-extra-file="$EXTRA_DEFAULTS")
fi

"$MARIADB" "${client_args[@]}" -e 'SELECT 1' >/dev/null 2>&1 \
    || die "cannot connect to MariaDB (if root needs a password, set EXTRA_DEFAULTS — see script header)"

"$MARIADB" "${client_args[@]}" -e "USE \`$DATABASE\`" >/dev/null 2>&1 \
    || die "database '$DATABASE' does not exist or is not accessible"

install -d -m 700 "$BACKUP_DIR"

# ---------------------------------------------------------------------------
# Dump
# ---------------------------------------------------------------------------
timestamp=$(date '+%Y%m%d-%H%M%S')
dest="$BACKUP_DIR/music-backup-$timestamp.sql.gz"
tmp=$(mktemp "$BACKUP_DIR/.music-backup-$timestamp.XXXXXX")
trap 'rm -f "$tmp"' EXIT

log "dumping database '$DATABASE' -> $dest"

# --single-transaction: consistent InnoDB snapshot without locking the app.
# --quick: stream rows without buffering the whole table in memory.
if ! "$MARIADB_DUMP" "${client_args[@]}" --single-transaction --quick --lock-tables=false "$DATABASE" | gzip > "$tmp"; then
    die "mariadb-dump failed; no backup was written" 2
fi

# ---------------------------------------------------------------------------
# Verify
# ---------------------------------------------------------------------------
gzip -t "$tmp" || die "backup failed gzip integrity check" 2
zgrep -q '^-- Dump completed' "$tmp" \
    || die "backup is missing the 'Dump completed' marker — dump likely truncated; no backup was written" 2

# Only publish the dump once it has passed verification.
mv "$tmp" "$dest"
trap - EXIT

# ---------------------------------------------------------------------------
# Prune: keep the newest KEEP dumps matching our exact filename pattern.
# ---------------------------------------------------------------------------
shopt -s nullglob
backups=("$BACKUP_DIR"/music-backup-*.sql.gz)
if (( ${#backups[@]} > KEEP )); then
    # Filenames embed a lexicographically sortable timestamp, so name order == age order.
    mapfile -d '' -t sorted < <(printf '%s\0' "${backups[@]}" | sort -z)
    for old in "${sorted[@]:0:${#sorted[@]}-KEEP}"; do
        rm -f -- "$old"
        log "pruned old backup: $old"
    done
fi

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------
size=$(du -h "$dest" | cut -f1)
remaining=("$BACKUP_DIR"/music-backup-*.sql.gz)
log "backup complete: $dest ($size)"
log "backups retained in $BACKUP_DIR: ${#remaining[@]} (KEEP=$KEEP)"
