import { defineConfig, devices } from '@playwright/test'

/**
 * E2E da Fase 11 (Pós-Encontro) contra o stack real: browser → frontend (vite) → backend Spring →
 * PostgreSQL, com login de verdade no Keycloak. Nada é mockado.
 *
 * Pré-requisitos (ver e2e/README.md): PostgreSQL e Keycloak de pé pelo compose de E2E e o backend
 * rodando em http://localhost:8080. O `webServer` abaixo cuida apenas do frontend.
 *
 * Os testes compartilham a mesma paróquia semeada e escrevem no banco (encontros pós, presenças),
 * então rodam em série — `fullyParallel` desligado e um único worker.
 */
export default defineConfig({
  testDir: './e2e/specs',
  globalSetup: './e2e/support/global-setup.ts',
  fullyParallel: false,
  workers: 1,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? [['list'], ['html', { open: 'never' }]] : [['list']],
  timeout: 60_000,
  expect: { timeout: 10_000 },
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:5173',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    ...devices['Desktop Chrome'],
  },
  projects: [{ name: 'chromium' }],
  webServer: {
    command: 'npm run dev -- --port 5173 --strictPort',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
    env: {
      VITE_API_BASE_URL: 'http://localhost:8080/api/v1',
      VITE_KEYCLOAK_URL: 'http://localhost:8081',
      VITE_KEYCLOAK_REALM: 'segueme',
      VITE_KEYCLOAK_CLIENT_ID: 'segueme-frontend',
    },
  },
})
