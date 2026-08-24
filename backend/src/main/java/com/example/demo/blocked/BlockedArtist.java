package com.example.demo.blocked;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "blocked_artists")
class BlockedArtist {

  @SuppressWarnings("NullAway.Init")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, updatable = false)
  private Instant blockedAt;

  @SuppressWarnings("NullAway.Init")
  BlockedArtist() {}

  BlockedArtist(Long userId, String name) {
    this.userId = userId;
    this.name = name;
    this.blockedAt = Instant.now();
  }

  Long getId() {
    return this.id;
  }

  Long getUserId() {
    return this.userId;
  }

  String getName() {
    return this.name;
  }

  Instant getBlockedAt() {
    return this.blockedAt;
  }
}
