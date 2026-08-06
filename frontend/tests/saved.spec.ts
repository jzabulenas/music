import { expect, test } from '@playwright/test';

import { loginAs } from './helpers/auth';
import { generateRecommendations, recommendationCards } from './helpers/recommendations';

function uniqueEmail(): string {
  return `saved-${crypto.randomUUID().slice(0, 8)}@e2e.test`;
}

const EMPTY_STATE_MESSAGE = 'No saved artists yet. Head to Recommendations to discover new ones!';

test.beforeEach(async ({ page }) => {
  await loginAs(page, uniqueEmail());
});

test('saved page shows an empty state message when no artists saved', async ({ page }) => {
  await page.goto('/app/saved');

  await expect(page.getByText(EMPTY_STATE_MESSAGE)).toBeVisible();
});

test('after saving an artist via the recommendations flow, it appears on the saved page', async ({
  page,
}) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const artistName = await card.getByTestId('artist-name').innerText();
  await card.getByRole('button', { name: 'Save for later' }).click();

  await page.goto('/app/saved');

  await expect(page.getByTestId('artist-name').filter({ hasText: artistName })).toBeVisible();
});

test('removing a saved artist removes it from the list and shows the empty state again', async ({
  page,
}) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const artistName = await card.getByTestId('artist-name').innerText();
  await card.getByRole('button', { name: 'Save for later' }).click();

  await page.goto('/app/saved');
  await expect(page.getByTestId('artist-name').filter({ hasText: artistName })).toBeVisible();

  await page.getByRole('button', { name: 'Remove' }).click();

  await expect(page.getByTestId('artist-name').filter({ hasText: artistName })).not.toBeVisible();
  await expect(page.getByText(EMPTY_STATE_MESSAGE)).toBeVisible();
});
