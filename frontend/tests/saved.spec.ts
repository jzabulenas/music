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

test('a removed saved artist is not suggested in the next generation', async ({ page }) => {
  test.setTimeout(90_000);

  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const artistName = await card.getByTestId('artist-name').innerText();
  await card.getByRole('button', { name: 'Save for later' }).click();

  await page.goto('/app/saved');
  await page.getByRole('button', { name: 'Remove' }).click();
  await expect(page.getByText(EMPTY_STATE_MESSAGE)).toBeVisible();

  await page.goto('/app/recommendations');
  const generation = page.waitForResponse(
    (response) =>
      response.url().endsWith('/api/v1/recommendations/generate') &&
      response.request().method() === 'POST',
  );
  await page.getByRole('button', { name: 'Generate recommendations' }).click();
  await generation;

  await expect(recommendationCards(page)).toHaveCount(5);
  await expect(recommendationCards(page).filter({ hasText: artistName })).toHaveCount(0);
});

test('liking a saved artist moves it to My Artists and out of the saved list', async ({ page }) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const artistName = await card.getByTestId('artist-name').innerText();
  await card.getByRole('button', { name: 'Save for later' }).click();

  await page.goto('/app/saved');
  await expect(page.getByTestId('artist-name').filter({ hasText: artistName })).toBeVisible();

  await page.getByRole('button', { name: 'Like' }).click();

  await expect(page.getByTestId('artist-name').filter({ hasText: artistName })).not.toBeVisible();
  await expect(page.getByText(EMPTY_STATE_MESSAGE)).toBeVisible();

  await page.goto('/app/artists');
  await expect(page.getByRole('listitem').filter({ hasText: artistName })).toBeVisible();
  await expect(page.getByText('4 artists added')).toBeVisible();
});
