import { expect, test } from '@playwright/test';

import { loginAs } from './helpers/auth';

function uniqueEmail(): string {
  return `nav-${crypto.randomUUID().slice(0, 8)}@e2e.test`;
}

test.beforeEach(async ({ page }) => {
  await loginAs(page, uniqueEmail());
});

test('Artists nav link navigates to the artists page', async ({ page }) => {
  await page.goto('/app/recommendations');
  await page.getByRole('link', { name: 'Artists' }).click();

  await expect(page).toHaveURL(/\/app\/artists/);
});

test('Recommendations nav link navigates to the recommendations page', async ({ page }) => {
  await page.getByRole('link', { name: 'Recommendations' }).click();

  await expect(page).toHaveURL(/\/app\/recommendations/);
});

test('Saved nav link navigates to the saved page', async ({ page }) => {
  await page.getByRole('link', { name: 'Saved' }).click();

  await expect(page).toHaveURL(/\/app\/saved/);
});

test('the active nav link has a distinct visual style from inactive links', async ({ page }) => {
  const activeLink = page.getByRole('link', { name: 'Artists' });
  const inactiveLink = page.getByRole('link', { name: 'Recommendations' });

  const [activeColor, inactiveColor] = await Promise.all([
    activeLink.evaluate((el) => getComputedStyle(el).backgroundColor),
    inactiveLink.evaluate((el) => getComputedStyle(el).backgroundColor),
  ]);

  expect(activeColor).not.toBe(inactiveColor);
});
