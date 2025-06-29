import { test, expect } from '@playwright/test';
import {text} from "node:stream/consumers";

test('home page loads', async ({ page }) => {
  await page.goto('/');

  // Expect a title "to contain" a substring.
  await expect(page).toHaveTitle(/MyLib/);
});

test.describe.serial("Sign Up", () =>{
  test("Sign Up", async ({ page }) =>{
    await page.goto("/");
    await page.click('text=Account');
    await page.click('text=Sign Up');
    await page.fill('#usernameInput', 'testuser1');
    await page.fill('#passwordInput', 'testpassword1');
    await page.click('button:has-text("Sign Up")');
    await page.waitForURL('**/');
    await expect(page.locator('text=Saved books')).toBeVisible();
    await page.click('text=Account');
    await expect(page.locator('text=/Logged in as:\\s*testuser1/')).toBeVisible();
  });




})

test.describe.serial("login", () =>{

})

