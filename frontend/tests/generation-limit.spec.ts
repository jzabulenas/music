import { expect, test } from '@playwright/test';

import { loginAs } from './helpers/auth';
import { generateRecommendations } from './helpers/recommendations';

function uniqueEmail(): string {
  return `limit-${crypto.randomUUID().slice(0, 8)}@e2e.test`;
}

test.beforeEach(async ({ page }) => {
  await loginAs(page, uniqueEmail());
  await page.goto('/app/recommendations');
});

test('a new user sees five generation requests left today', async ({ page }) => {
  await expect(page.getByText('5 generation requests left today')).toBeVisible();
});

test('after generating recommendations the counter decreases to four', async ({ page }) => {
  await generateRecommendations(page);

  await expect(page.getByText('4 generation requests left today')).toBeVisible();
});
