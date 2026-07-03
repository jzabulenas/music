import { type Page } from '@playwright/test';

import { getMagicLink } from './mailpit';

export async function loginAs(page: Page, email: string): Promise<void> {
  await page.goto('/login');
  await page.getByLabel('Email address').fill(email);
  await page.getByRole('button', { name: 'Send magic link' }).click();
  await page.getByRole('heading', { name: 'Check your inbox' }).waitFor();

  const magicLink = await getMagicLink(email);

  await page.goto(magicLink);
  await page.waitForURL('**/app/artists');
}
