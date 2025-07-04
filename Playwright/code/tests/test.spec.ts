import {test, expect, Page} from '@playwright/test';
import {text} from "node:stream/consumers";

test('home page loads', async ({ page }) => {
  await page.goto('/');

  // Expect a title "to contain" a substring.
  await expect(page).toHaveTitle(/MyLib/);
});

test.describe("Sign Up", () =>{
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
});

test.describe.serial("Login Logout", () =>{
  const randomUser = `user${Date.now()}`; // do this separately so I don't have to log out before logging in
  test("Create user" , async ({page}) =>{
    await signUpNewUser(randomUser, page);
  });

  test("Log in and out", async ({page}) =>{
    await page.goto("/");
    await page.click('text=Account');
    await page.click('text=Login');
    await page.fill('#usernameInput', randomUser);
    await page.fill('#passwordInput', 'testpassword1');
    await page.click('button:has-text("Login")');
    await page.waitForURL('**/');
    await expect(page.locator('text=Saved books')).toBeVisible();
    await page.click('text=Account');
    await expect(page.locator(`text=/Logged in as:\\s*${randomUser}/`)).toBeVisible();
    await page.click('button:has-text("Logout")');
    await page.click('text=Account');
    await expect(page.locator(`text="Login"`)).toBeVisible();
    await expect(page.locator(`text="Sign Up"`)).toBeVisible();
  });
});

test("Search and go to details", async ({page}) =>{
  await page.goto("/search");
  await page.fill('#inputKeywords', 'Mass Effect');
  await page.click('button:has-text("Search")');
  await expect(page.locator('text=Mass Effect Ascension')).toBeVisible();
  await expect(page.locator('table tbody tr')).toHaveCount(10);
  await page.locator('tr', { hasText: 'Mass Effect Ascension' }).locator('text=Details').click();
  await page.waitForURL("**/book/OL23106658M")
  await expect(page.locator('text="Mass effect - Ascension"')).toBeVisible();
});

test("Search and add to library", async ({page}) => {
  const randomUser = `user${Date.now()}`;
  await signUpNewUser(randomUser, page);
  await searchLoggedIn(page);
  await page.locator('tr', {hasText: 'Mass Effect Ascension'}).locator('text=Add to library ').click();
  await expect(page.locator('text=Loading...')).toBeHidden();
  await expect(page.locator('tr', {hasText: 'Mass Effect Ascension'}).locator('text=Add to library')).toBeHidden();
  await expect(page.locator('tr', {hasText: 'Mass Effect Ascension'}).locator('text=Add to wishlist')).toBeHidden();
});

test("Search and add to wishlist", async ({page}) =>{
  const randomUser = `user${Date.now()}`;
  await signUpNewUser(randomUser, page);
  await searchLoggedIn(page);
  await page.locator('tr', { hasText: 'Mass Effect Ascension' }).locator('text=Add to wishlist').click();
  await expect(page.locator('text=Loading...')).toBeHidden();
  await expect(page.locator('tr', { hasText: 'Mass Effect Ascension' }).locator('text=Add to library')).toBeVisible();
  await expect(page.locator('tr', { hasText: 'Mass Effect Ascension' }).locator('text=Add to wishlist')).toBeHidden();
});

test("Details add to library and change rating + status and delete", async ({page}) =>{
  await getDetailsLoggedIn(page);
  await page.click('button:has-text("Add to library")');
  await expect(page.locator('text=Add to library')).toBeHidden();
  await expect(page.locator('text=Add to wishlist')).toBeHidden();
  await expect(page.locator('text=/Your Rating:\\s*Not rated/')).toBeVisible();
  await expect(page.locator('text=UNREAD')).toBeVisible();

  await page.locator('text=Your Rating:').locator('..').locator('text=Edit').click();
  await expect(page.locator('text=Save')).toBeVisible();
  await page.locator('text=Your Rating:').locator('..').locator('select').selectOption('3');
  await page.locator('text=Save').click();
  await expect(page.locator('text=/Your Rating:\\s*3\\s\/\\s5/')).toBeVisible();
  await expect(page.locator('text=/Your Status:\\s*UNREAD/')).toBeVisible();

  await page.locator('text=Your Status:').locator('..').locator('text=Edit').click();
  await expect(page.locator('text=Save')).toBeVisible();
  await page.locator('text=Your Status:').locator('..').locator('select').selectOption('READING');
  await page.locator('text=Save').click();
  await expect(page.locator('text=/Your Status:\\s*READING/')).toBeVisible();


  await page.locator("text=Delete from library").click();
  await expect(page.locator('text=Add to library')).toBeVisible();
  await expect(page.locator('text=Add to wishlist')).toBeVisible();
});

test("Details add to wishlist and delete", async ({page}) =>{
  await getDetailsLoggedIn(page);
  await page.click('button:has-text("Add to wishlist")');
  await expect(page.locator('text=Add to library')).toBeVisible();
  await expect(page.locator('text=Add to wishlist')).toBeHidden();

  await page.locator("text=Delete from wishlist").click();
  await expect(page.locator('text=Add to library')).toBeVisible();
  await expect(page.locator('text=Add to wishlist')).toBeVisible();
});


test("Library go to details", async ({page}) => {
  await addBookToLibrary(page);
  await page.goto("/library");
  await page.locator("text=Details").click()
  await expect(page.locator("text=Mass effect - Ascension")).toBeVisible()
  await expect(page.locator("text=Delete from library")).toBeVisible();
})

test("Library delete", async ({page}) =>{
  await addBookToLibrary(page);
  await page.goto("/library");
  await expect(page.locator("text=Mass effect - Ascension")).toBeVisible()
  await expect(page.locator("text=Delete from library")).toBeVisible();
  await page.locator("text=Delete from library").click();
  await expect(page.locator("text=Mass effect - Ascension")).toBeHidden();
})

test("Wishlist go to details", async ({page}) => {
  await addBookToWishlist(page);
  await page.goto("/wishlist");
  await page.locator("text=Details").click()
  await expect(page.locator("text=Mass effect - Ascension")).toBeVisible()
  await expect(page.locator("text=Add to library")).toBeVisible();
  await expect(page.locator("text=Delete from wishlist")).toBeVisible();
})

test("Wishlist delete", async ({page}) =>{
  await addBookToWishlist(page);
  await page.goto("/wishlist");
  await expect(page.locator("text=Mass effect - Ascension")).toBeVisible()
  await expect(page.locator("text=Delete from wishlist")).toBeVisible();
  await page.locator("text=Delete from wishlist").click();
  await expect(page.locator("text=Mass effect - Ascension")).toBeHidden();
})

test.describe.serial("All Books", () =>{

  //Add a book to the library so that another user can see it on the allbooks page
  test("Add book to database", async ({page}) =>{
    await addBookToLibrary(page);
  });

  test("Go to details", async ({page}) =>{
    await page.goto("/");
    await expect(page.locator("text=Mass effect - Ascension")).toBeVisible()
    await page.locator("text=Details").click();
    await expect(page.locator("text=Mass effect - Ascension")).toBeVisible()
  });

  test("Add to library", async ({page})=>{
    await alLBooksSignedIn(page);
    await page.locator("text=Add to library ").click();
    await expect(page.locator("text=Add to library")).toBeHidden();
    await expect(page.locator("text=Add to wishlist")).toBeHidden();
  })

  test("Add to wishlist", async ({page})=>{
    await alLBooksSignedIn(page);
    await page.locator("text=Add to wishlist ").click();
    await expect(page.locator("text=Add to library")).toBeVisible();
    await expect(page.locator("text=Add to wishlist")).toBeHidden();
  })

});

const signUpNewUser = ( async (username:string, page:Page)=>{
  await page.goto("/");
  await page.click('text=Account');
  await page.click('text=Sign Up');
  await page.fill('#usernameInput', username);
  await page.fill('#passwordInput', 'testpassword1');
  await page.click('button:has-text("Sign Up")');
});

const searchLoggedIn = (async (page: Page) =>{
  await expect(page.locator('text="Saved books"')).toBeVisible()
  await page.goto("/search");
  await page.fill('#inputKeywords', 'Mass Effect');
  await page.click('button:has-text("Search")');
  await expect(page.locator('text=Mass Effect Ascension')).toBeVisible();
  await expect(page.locator('tr', {hasText: 'Mass Effect Ascension'}).locator('text=Details')).toBeVisible();
  await expect(page.locator('tr', { hasText: 'Mass Effect Ascension' }).locator('text=Add to library')).toBeVisible();
  await expect(page.locator('tr', { hasText: 'Mass Effect Ascension' }).locator('text=Add to wishlist')).toBeVisible();
});


const getDetailsLoggedIn = (async (page: Page)=>{
  const randomUser = `user${Date.now()}`;
  await signUpNewUser(randomUser, page);
  await expect(page.locator('text="Saved books"')).toBeVisible();
  await page.goto("/book/OL23106658M");
  await expect(page.locator('text=Add to library')).toBeVisible();
  await expect(page.locator('text=Add to wishlist')).toBeVisible();
});

const addBookToLibrary = (async (page: Page)=>{
  await getDetailsLoggedIn(page);
  await page.locator('text=Add to library').click();
});

const addBookToWishlist = (async (page: Page)=>{
  await getDetailsLoggedIn(page);
  await page.locator('text=Add to wishlist').click();
});

const alLBooksSignedIn = (async (page: Page)=>{
  const randomUser = `user${Date.now()}`;
  await signUpNewUser(randomUser, page);
  await expect(page.locator('text="Saved books"')).toBeVisible();
  await expect(page.locator("text=Add to library")).toBeVisible();
  await expect(page.locator("text=Add to wishlist")).toBeVisible();
});

