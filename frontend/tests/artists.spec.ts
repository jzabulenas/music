import { expect, test } from '@playwright/test';

import { addArtist } from './helpers/artists';
import { loginAs } from './helpers/auth';

function uniqueEmail(): string {
  return `artists-${crypto.randomUUID().slice(0, 8)}@e2e.test`;
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

test('liked artists are listed in alphabetical order', async ({ page }) => {
  await addArtist(page, 'Radiohead');
  await addArtist(page, 'Portishead');
  await addArtist(page, 'Autechre');

  await expect(page.getByRole('listitem')).toHaveText([/Autechre/, /Portishead/, /Radiohead/]);
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

test('clicking the remove button opens a confirmation dialog without deleting the artist', async ({
  page,
}) => {
  await addArtist(page, 'Radiohead');
  await page.getByRole('button', { name: 'Remove Radiohead' }).click();

  const dialog = page.getByRole('dialog');
  await expect(dialog).toBeVisible();
  await expect(dialog.getByRole('heading', { name: 'Remove Radiohead?' })).toBeVisible();
  await expect(page.getByRole('listitem').filter({ hasText: 'Radiohead' })).toBeVisible();
  await expect(page.getByText('1 artist added')).toBeVisible();
});

test('confirming in the dialog deletes the artist from the list', async ({ page }) => {
  await addArtist(page, 'Radiohead');
  await page.getByRole('button', { name: 'Remove Radiohead' }).click();
  await page.getByRole('dialog').getByRole('button', { name: 'Remove' }).click();

  await expect(page.getByRole('dialog')).not.toBeVisible();
  await expect(page.getByRole('listitem').filter({ hasText: 'Radiohead' })).not.toBeVisible();
  await expect(page.getByText('0 artists added')).toBeVisible();
});

test('cancelling in the dialog keeps the artist in the list', async ({ page }) => {
  await addArtist(page, 'Radiohead');
  await page.getByRole('button', { name: 'Remove Radiohead' }).click();
  await page.getByRole('dialog').getByRole('button', { name: 'Cancel' }).click();

  await expect(page.getByRole('dialog')).not.toBeVisible();
  await expect(page.getByRole('listitem').filter({ hasText: 'Radiohead' })).toBeVisible();
  await expect(page.getByText('1 artist added')).toBeVisible();
});
