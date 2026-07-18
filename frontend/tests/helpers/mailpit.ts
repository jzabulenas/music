const MAILPIT_URL = 'http://localhost:8026';
const POLL_INTERVAL_MS = 500;
const TIMEOUT_MS = 10_000;

interface MailpitMessage {
  ID: string;
  To: Array<{ Address: string }>;
}

interface MailpitMessageList {
  messages: MailpitMessage[];
}

interface MailpitMessageDetail {
  HTML: string;
}

export async function getMagicLink(email: string): Promise<string> {
  const deadline = Date.now() + TIMEOUT_MS;

  while (Date.now() < deadline) {
    const res = await fetch(`${MAILPIT_URL}/api/v1/messages`);
    const data: MailpitMessageList = await res.json();

    const message = data.messages?.find((m) => m.To.some((to) => to.Address === email));

    if (message) {
      const detailRes = await fetch(`${MAILPIT_URL}/api/v1/message/${message.ID}`);
      const detail: MailpitMessageDetail = await detailRes.json();

      await deleteMessage(message.ID);

      const match = detail.HTML.match(/https?:[^"'\s]*\/login\/ott\?token=[^"'\s<]*/);

      if (!match) {
        throw new Error(`Magic link not found in email body for ${email}`);
      }

      return match[0];
    }

    await new Promise((resolve) => setTimeout(resolve, POLL_INTERVAL_MS));
  }

  throw new Error(`No email received for ${email} within ${TIMEOUT_MS}ms`);
}

async function deleteMessage(id: string): Promise<void> {
  await fetch(`${MAILPIT_URL}/api/v1/messages`, {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ IDs: [id] }),
  });
}
