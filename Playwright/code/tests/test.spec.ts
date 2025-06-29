import { test, expect } from '@playwright/test';
import {text} from "node:stream/consumers";

test('home page loads', async ({ page }) => {
  await page.goto('/');

  // Expect a title "to contain" a substring.
  await expect(page).toHaveTitle(/MyLib/);
});

test.describe.serial("Sign Up", () =>{
  const randomUser = `user${Date.now()}`; // because I need three different user for three browsers...
  test("Sign Up", async ({ page }) =>{
    page.on('console', msg => {
      console.log('BROWSER LOG:', msg.text());
    });

    await page.goto("/");
    await page.click('text=Account');
    await page.click('text=Sign Up');
    await page.fill('#usernameInput', randomUser);
    await page.fill('#passwordInput', 'testpassword1');
    await page.click('button:has-text("Sign Up")');
    await page.waitForURL('**/');
    await expect(page.locator('text=Saved books')).toBeVisible();
    await page.click('text=Account');
    await expect(page.locator(`text=/Logged in as:\\s*${randomUser}/`)).toBeVisible();
  });
})

test.describe.serial("login", () =>{

})

