import { test as setup } from '@playwright/test';

import { loginAs } from './helpers/auth';

const authFile = 'playwright/.auth/user.json';

setup('authenticate', async ({ page }) => {
  await loginAs(page, 'setup@e2e.test');
  await page.context().storageState({ path: authFile });
});
