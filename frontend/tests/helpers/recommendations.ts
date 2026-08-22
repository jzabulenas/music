import { type Page, expect, test } from '@playwright/test';

import { addArtist } from './artists';

export function recommendationCards(page: Page) {
  return page.getByRole('list').getByRole('listitem');
}

export async function generateRecommendations(page: Page): Promise<void> {
  test.setTimeout(60_000);

  await addArtist(page, 'Radiohead');
  await addArtist(page, 'Portishead');
  await addArtist(page, 'Massive Attack');

  const initialLoad = page.waitForResponse(
    (response) =>
      response.url().endsWith('/api/v1/recommendations') && response.request().method() === 'GET',
  );
  await page.goto('/app/recommendations');
  await initialLoad;

  await page.getByRole('button', { name: 'Generate recommendations' }).click();
  await expect(recommendationCards(page)).toHaveCount(5, { timeout: 45_000 });
}
