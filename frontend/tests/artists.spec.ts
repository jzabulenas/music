import { type Page, expect, test } from '@playwright/test';

import { loginAs } from './helpers/auth';

function uniqueEmail(): string {
  return `artists-${crypto.randomUUID().slice(0, 8)}@e2e.test`;
}

async function addArtist(page: Page, name: string): Promise<void> {
  await page.getByLabel('Artist name').fill(name);
  await page.getByRole('button', { name: 'Add' }).click();
}

test.beforeEach(async ({ page }) => {
  await loginAs(page, uniqueEmail());
});

test('artists page loads with an empty list initially', async ({ page }) => {
  await expect(page.getByRole('heading', { name: 'My Artists' })).toBeVisible();
  await expect(page.getByText('0 artists added')).toBeVisible();
  await expect(page.getByRole('list', { name: 'Liked artists' })).not.toBeVisible();
});

test('adding an artist shows it as a chip in the list', async ({ page }) => {
  await addArtist(page, 'Radiohead');

  await expect(page.getByRole('listitem').filter({ hasText: 'Radiohead' })).toBeVisible();
  await expect(page.getByText('1 artist added')).toBeVisible();
});

test('adding a second artist appends to the list', async ({ page }) => {
  await addArtist(page, 'Radiohead');
  await addArtist(page, 'Portishead');

  await expect(page.getByRole('listitem').filter({ hasText: 'Radiohead' })).toBeVisible();
  await expect(page.getByRole('listitem').filter({ hasText: 'Portishead' })).toBeVisible();
  await expect(page.getByText('2 artists added')).toBeVisible();
});

test('submitting a blank name shows a validation error', async ({ page }) => {
  await page.getByRole('button', { name: 'Add' }).click();

  await expect(page.getByText('Artist name is required')).toBeVisible();
});

test('adding an artist that already exists shows an error', async ({ page }) => {
  await addArtist(page, 'Radiohead');
  await addArtist(page, 'Radiohead');

  await expect(page.getByRole('alert')).toBeVisible();
});

test('clicking the remove button on a chip removes it from the list', async ({ page }) => {
  await addArtist(page, 'Radiohead');
  await page.getByRole('button', { name: 'Remove Radiohead' }).click();

  await expect(page.getByRole('listitem').filter({ hasText: 'Radiohead' })).not.toBeVisible();
  await expect(page.getByText('0 artists added')).toBeVisible();
});
