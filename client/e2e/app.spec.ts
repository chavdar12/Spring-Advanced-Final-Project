import {expect, Page, test} from '@playwright/test';

const unique = () =>
  `e2e${Date.now().toString(36)}${Math.floor(Math.random() * 1000)}`.slice(0, 12);

/** One API-created account for the logged-in specs: auth endpoints are rate-limited at the gateway. */
let session: {token: string; username: string; roles: string[]};

test.beforeAll(async ({request}) => {
  const username = unique();
  await expect(
    await request.post('/api/auth/signup', {
      data: {username, email: `${username}@mail.bg`, password: 'secret123'},
    }),
  ).toBeOK();
  const res = await request.post('/api/auth/signin', {data: {username, password: 'secret123'}});
  await expect(res).toBeOK();
  const body = await res.json();
  session = {token: body.token, username, roles: body.roles};
});

async function loggedIn(page: Page) {
  await page.addInitScript(
    (s) => sessionStorage.setItem('trade-zone.session', JSON.stringify(s)),
    session,
  );
}

test('a visitor can register and log in', async ({page}) => {
  const username = unique();
  await page.goto('/');
  await page.getByRole('link', {name: 'Sign up'}).first().click();

  await page.getByLabel('Username').fill(username);
  await page.getByLabel('Email').fill(`${username}@mail.bg`);
  await page.getByLabel('Password', {exact: true}).fill('secret123');
  await page.getByLabel('Repeat password').fill('secret123');
  await page.getByRole('button', {name: 'Create account'}).click();

  await expect(page).toHaveURL(/\/auth\/signin$/);
  await page.getByLabel('Username').fill(username);
  await page.getByLabel('Password').fill('secret123');
  await page.getByRole('button', {name: 'Login'}).click();

  await expect(page.getByRole('link', {name: 'Advertisements'})).toBeVisible();
  await expect(page.getByRole('button', {name: 'Logout'})).toBeVisible();
});

test('protected pages redirect anonymous visitors to login', async ({page}) => {
  await page.goto('/user/profile');
  await expect(page).toHaveURL(/\/auth\/signin$/);
  await expect(page.getByRole('heading', {name: 'Login'})).toBeVisible();
});

test('the gateway rejects API calls without a valid bearer token', async ({request}) => {
  const anonymous = await request.get('/api/regions');
  expect(anonymous.status()).toBe(401);
  expect(anonymous.headers()['content-type']).toContain('application/problem+json');
  expect((await anonymous.json()).detail).toBe('Sign in to continue');
  expect(
    (await request.get('/api/regions', {headers: {Authorization: 'Bearer nope'}})).status(),
  ).toBe(401);
});

test('advertisements page lists categories and filters', async ({page}) => {
  await loggedIn(page);
  await page.goto('/advertisements');
  await expect(page).toHaveURL(/\/advertisements\/category\/All$/);
  await expect(page.getByRole('option', {name: 'All'})).toBeVisible();
  await expect(page.getByRole('option', {name: 'Vehicles'})).toBeVisible();
  await expect(
    page.getByRole('link', {name: 'Complete your profile to create advertisements'}),
  ).toBeVisible();

  await page.getByRole('option', {name: 'Vehicles'}).click();
  await expect(page).toHaveURL(/\/advertisements\/category\/Vehicles$/);
  await expect(page.getByText('0 results')).toBeVisible();
});

test('regions expand to show their towns', async ({page}) => {
  await loggedIn(page);
  await page.goto('/regions');
  const panels = page.locator('mat-expansion-panel');
  await expect(panels).toHaveCount(28);
  await panels.first().locator('mat-expansion-panel-header').click();
  await expect(panels.first().locator('mat-list-item').first()).not.toHaveText('Loading…');
});

test('a user can fill in their profile', async ({page}) => {
  await loggedIn(page);
  await page.goto('/user/profile');
  await page.getByRole('tab', {name: 'Edit info'}).click();

  await page.getByLabel('First name').fill('Ivan');
  await page.getByLabel('Last name').fill('Petrov');
  await page.getByLabel('Town').click();
  await page.getByRole('option').first().click();
  await page.getByLabel('About me').fill('Selling old stuff');
  await page.getByRole('button', {name: 'Save'}).click();

  await expect(page.getByText('Profile updated')).toBeVisible();
  await expect(page.getByText('Ivan Petrov')).toBeVisible();
});
