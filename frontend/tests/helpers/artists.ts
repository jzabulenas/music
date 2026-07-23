import { type Page } from '@playwright/test';

export async function addArtist(page: Page, name: string): Promise<void> {
  await page.goto('/app/artists');
  await page.getByLabel('Artist name').fill(name);
  await page.getByRole('button', { name: 'Add' }).click();
  await page.getByRole('listitem').filter({ hasText: name }).waitFor();
}
