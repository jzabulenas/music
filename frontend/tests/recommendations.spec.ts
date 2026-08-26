import { expect, test } from '@playwright/test';

import { addArtist } from './helpers/artists';
import { loginAs } from './helpers/auth';
import { generateRecommendations, recommendationCards } from './helpers/recommendations';

function uniqueEmail(): string {
  return `recs-${crypto.randomUUID().slice(0, 8)}@e2e.test`;
}

test.beforeEach(async ({ page }) => {
  await loginAs(page, uniqueEmail());
  await page.goto('/app/recommendations');
});

test('generate button is present on the recommendations page', async ({ page }) => {
  await expect(page.getByRole('button', { name: 'Generate recommendations' })).toBeVisible();
});

test('with 0 liked artists, generate button is disabled and shows a warning', async ({ page }) => {
  await expect(page.getByRole('button', { name: 'Generate recommendations' })).toBeDisabled();
  await expect(page.getByText('Add 3 more artists to generate recommendations')).toBeVisible();
});

test('with 1 liked artist, generate button is still disabled', async ({ page }) => {
  await addArtist(page, 'Radiohead');
  await page.goto('/app/recommendations');

  await expect(page.getByRole('button', { name: 'Generate recommendations' })).toBeDisabled();
  await expect(page.getByText('Add 2 more artists to generate recommendations')).toBeVisible();
});

test('with 2 liked artists, generate button is still disabled', async ({ page }) => {
  await addArtist(page, 'Radiohead');
  await addArtist(page, 'Portishead');
  await page.goto('/app/recommendations');

  await expect(page.getByRole('button', { name: 'Generate recommendations' })).toBeDisabled();
  await expect(page.getByText('Add 1 more artist to generate recommendations')).toBeVisible();
});

test('with 3 liked artists, generate button becomes enabled', async ({ page }) => {
  await addArtist(page, 'Radiohead');
  await addArtist(page, 'Portishead');
  await addArtist(page, 'Massive Attack');
  await page.goto('/app/recommendations');

  await expect(page.getByRole('button', { name: 'Generate recommendations' })).toBeEnabled();
});

test('clicking generate shows a loading indicator', async ({ page }) => {
  await addArtist(page, 'Radiohead');
  await addArtist(page, 'Portishead');
  await addArtist(page, 'Massive Attack');
  await page.goto('/app/recommendations');
  await page.getByRole('button', { name: 'Generate recommendations' }).click();

  await expect(page.getByRole('progressbar', { name: 'Generating recommendations' })).toBeVisible();
});

test('after loading, 5 recommendation cards are displayed', async ({ page }) => {
  await generateRecommendations(page);

  await expect(recommendationCards(page)).toHaveCount(5);
});

test('each card shows artist name, genre text, and reason text', async ({ page }) => {
  await generateRecommendations(page);

  const cards = await recommendationCards(page).all();

  for (const card of cards) {
    await expect(card.getByTestId('artist-name')).not.toBeEmpty();
    await expect(card.getByTestId('artist-genre')).not.toBeEmpty();
    await expect(card.getByTestId('reason')).not.toBeEmpty();
  }
});

test('clicking Save on a recommendation card triggers a success indication', async ({ page }) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  await card.getByRole('button', { name: 'Save for later' }).click();

  await expect(card.getByRole('button', { name: 'Saved' })).toBeDisabled();
});

test('clicking "Don\'t suggest again" blocks the artist without removing the card', async ({
  page,
}) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  await card.getByRole('button', { name: "Don't suggest again" }).click();

  await expect(card.getByRole('button', { name: "Won't suggest again" })).toBeDisabled();
  await expect(recommendationCards(page)).toHaveCount(5);
});

test('a blocked artist still shows "Won\'t suggest again" after reloading the page', async ({
  page,
}) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const artistName = await card.getByTestId('artist-name').textContent();
  await card.getByRole('button', { name: "Don't suggest again" }).click();
  await expect(card.getByRole('button', { name: "Won't suggest again" })).toBeDisabled();

  await page.reload();

  const cardAfterReload = recommendationCards(page).filter({ hasText: artistName ?? '' });
  await expect(cardAfterReload.getByRole('button', { name: "Won't suggest again" })).toBeDisabled();
});

test('a saved artist still shows "Saved" after reloading the page', async ({ page }) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const artistName = await card.getByTestId('artist-name').textContent();
  await card.getByRole('button', { name: 'Save for later' }).click();
  await expect(card.getByRole('button', { name: 'Saved' })).toBeDisabled();

  await page.reload();

  const cardAfterReload = recommendationCards(page).filter({ hasText: artistName ?? '' });
  await expect(cardAfterReload.getByRole('button', { name: 'Saved' })).toBeDisabled();
});
