import { expect, test } from '@playwright/test';

import { loginAs } from './helpers/auth';

test.use({ storageState: { cookies: [], origins: [] } });

function uniqueEmail(): string {
  return `auth-${crypto.randomUUID().slice(0, 8)}@e2e.test`;
}

test('unauthenticated user visiting /app/artists is redirected to /login', async ({ page }) => {
  await page.goto('/app/artists');

  await expect(page).toHaveURL(/\/login/);
});

test('login page shows email input and submit button', async ({ page }) => {
  await page.goto('/login');

  await expect(page.getByLabel('Email address')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Send magic link' })).toBeVisible();
});

test('submitting a valid email shows the check your inbox confirmation', async ({ page }) => {
  await page.goto('/login');
  await page.getByLabel('Email address').fill(uniqueEmail());
  await page.getByRole('button', { name: 'Send magic link' }).click();

  await expect(page.getByRole('heading', { name: 'Check your inbox' })).toBeVisible();
});

test('completing the magic link flow lands the user on the artists page', async ({ page }) => {
  await loginAs(page, uniqueEmail());

  await expect(page).toHaveURL(/\/app\/artists/);
});

test('logout button logs out and redirects to login', async ({ page }) => {
  await loginAs(page, uniqueEmail());
  await page.getByRole('button', { name: 'Logout' }).click();

  await expect(page).toHaveURL(/\/login/);
});

test('after logout, visiting /app/artists redirects to login', async ({ page }) => {
  await loginAs(page, uniqueEmail());
  await page.getByRole('button', { name: 'Logout' }).click();
  await page.waitForURL(/\/login/);

  await page.goto('/app/artists');

  await expect(page).toHaveURL(/\/login/);
});
