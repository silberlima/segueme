/** Endereços e credenciais do stack de E2E — todos sobrescrevíveis por variável de ambiente. */
export const STACK = {
  frontendUrl: process.env.E2E_BASE_URL ?? 'http://localhost:5173',
  apiUrl: process.env.E2E_API_URL ?? 'http://localhost:8080/api/v1',
  keycloakUrl: process.env.E2E_KEYCLOAK_URL ?? 'http://localhost:8081',
  realm: 'segueme',
  clientId: 'segueme-frontend',
  admin: {
    username: process.env.KEYCLOAK_ADMIN ?? 'admin',
    password: process.env.KEYCLOAK_ADMIN_PASSWORD ?? 'admin',
  },
  postgres: {
    container: process.env.E2E_POSTGRES_CONTAINER ?? 'backend-postgres-1',
    database: process.env.DB_NAME ?? 'segueme',
    username: process.env.DB_USERNAME ?? 'segueme',
  },
}

/**
 * Usuário de teste provisionado no Keycloak pelo global setup. Recebe POS_ENCONTRO (dono do módulo)
 * e PAROQUIA, e o atributo `paroquia_id` da paróquia semeada — o backend resolve o escopo
 * territorial desses perfis por essa claim (`EscopoTerritorialResolver`).
 */
export const USUARIO_E2E = {
  username: 'e2e.posencontro@segueme.test',
  email: 'e2e.posencontro@segueme.test',
  password: 'e2e-posencontro',
  nome: 'Equipe Pós-Encontro E2E',
  roles: ['POS_ENCONTRO', 'PAROQUIA'],
}

/**
 * Segundo usuário, com perfil Setorial, usado pela suíte da Fase 14 para decidir a transferência
 * que o perfil Paróquia solicita. É um usuário à parte porque `EscopoTerritorialResolver` prioriza
 * o perfil de maior alcance: dar SETORIAL ao usuário acima resolveria o escopo dele como "de setor"
 * e quebraria o Painel da Paróquia, que exige escopo de paróquia.
 */
export const USUARIO_SETORIAL_E2E = {
  username: 'e2e.setorial@segueme.test',
  email: 'e2e.setorial@segueme.test',
  password: 'e2e-setorial',
  primeiroNome: 'Equipe',
  sobrenome: 'Setorial E2E',
  roles: ['SETORIAL', 'PAROQUIA'],
}

/**
 * Massa de teste com ids fixos: o seed é idempotente (apaga e recria estes registros) para que a
 * suíte possa rodar quantas vezes for preciso sem sujar o banco de desenvolvimento.
 */
export const MASSA = {
  dioceseId: '11111111-1111-4111-8111-000000000001',
  setorId: '11111111-1111-4111-8111-000000000002',
  paroquiaId: '11111111-1111-4111-8111-000000000003',
  paroquiaNome: 'Paróquia E2E Pós-Encontro',
  /** Destino das transferências da Fase 14 — no mesmo setor, para o Setorial de E2E decidir. */
  paroquiaDestinoId: '11111111-1111-4111-8111-000000000004',
  paroquiaDestinoNome: 'Paróquia E2E Destino',
  jovens: [
    { id: '11111111-1111-4111-8111-000000000101', nome: 'Ana Assídua E2E' },
    { id: '11111111-1111-4111-8111-000000000102', nome: 'Bruno Faltante E2E' },
  ],
}
