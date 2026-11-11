import {defineConfig, devices} from '@playwright/test';

// Runs against the full docker compose stack (gateway on :8080): `docker compose up -d && npm run e2e`
export default defineConfig({
  testDir: 'e2e',
  fullyParallel: false, // the gateway rate-limits auth calls per IP
  retries: process.env['CI'] ? 1 : 0,
  reporter: process.env['CI'] ? 'github' : 'list',
  use: {
    baseURL: process.env['E2E_BASE_URL'] ?? 'http://localhost:8080',
    trace: 'retain-on-failure',
  },
  projects: [{name: 'chromium', use: {...devices['Desktop Chrome']}}],
});
