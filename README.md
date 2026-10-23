# Segue-me

Sistema de gestão do **Movimento Segue-me** — movimento de jovens da Igreja Católica. Organiza a
hierarquia territorial (**Nacional → Diocese → Setorial → Paróquia**) e todo o ciclo de vida de um
**Encontro**: inscrições, montagem de equipes, convites, transferências entre paróquias, financeiro,
palestras e pós-encontro.

O sistema atende **10 perfis de acesso**, cada um com seu próprio painel e menu, e mantém um
cadastro nacional único de pessoas cujo histórico nunca é apagado.

## Stack

| Camada | Tecnologias |
|---|---|
| Backend | Java 25, Spring Boot 4.1, Spring Data JPA, Spring Security (OAuth2 Resource Server / JWT), Liquibase, MapStruct, springdoc-openapi |
| Banco | PostgreSQL 16 |
| Identidade | Keycloak 26 (realm `segueme` importado automaticamente) |
| Frontend | React 19, TypeScript, Vite, Tailwind CSS 4, TanStack Query, React Router, Axios |
| Testes | JUnit 5, Mockito, Testcontainers, JaCoCo (backend) · Playwright E2E contra o stack real (frontend) |
| Observabilidade | Actuator, Micrometer, Prometheus, Grafana (dashboard e alertas provisionados) |
| CI | GitHub Actions — `backend-ci.yml` e `frontend-ci.yml` |

## Estrutura do repositório

```
segueme/
├── backend/          # API Spring Boot (DDD: api → application → domain → infrastructure)
│   └── observabilidade/  # Prometheus + Grafana
├── frontend/         # SPA React organizada por domínio (src/domains/<dominio>)
│   └── e2e/          # testes Playwright
├── docs/             # documentação funcional: RF, RN, casos de uso, entidades, pendências
├── skills/           # padrões técnicos (arquitetura, API, segurança, testes, CI/CD…)
├── prototype/        # protótipo navegável de alta fidelidade (referência de UX)
└── plano-implementacao.md  # roteiro de implementação em fases
```

Módulos de domínio do backend: `acesso`, `hierarquia`, `pessoas`, `encontros`, `inscricoes`,
`montagem`, `convites`, `movimentacao`, `financeiro`, `conteudo`, `posencontro`, `dashboards`,
`relatorios`, `notificacao`.

## Como rodar localmente

### Pré-requisitos

- Java 25
- Node.js 22
- Docker e Docker Compose

### 1. Infraestrutura (PostgreSQL + Keycloak)

O Keycloak emite tokens com `iss: http://keycloak:8081/...`, então a máquina precisa resolver o
nome `keycloak`. Adicione ao `/etc/hosts` uma vez:

```
127.0.0.1 keycloak
```

Depois suba os containers:

```bash
cd backend
docker compose up -d postgres keycloak
```

| Serviço | Endereço |
|---|---|
| PostgreSQL | `localhost:5432` (db/usuário/senha: `segueme`) |
| Keycloak | <http://localhost:8081> (admin: `admin`/`admin`) |

### 2. Usuário de desenvolvimento

O realm vem com as 10 roles, mas sem usuários. No console do Keycloak
(<http://localhost:8081/admin>, realm `segueme`), crie um usuário com senha e atribua a ele as
roles dos perfis que quiser testar.

### 3. Backend

```bash
cd backend
./mvnw spring-boot:run
```

- API: <http://localhost:8080/api/v1>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Health: <http://localhost:8080/actuator/health>

As migrations Liquibase rodam na subida. Para subir tudo em containers (incluindo a app):
`docker compose up -d --build`.

### 4. Frontend

```bash
cd frontend
cp .env.example .env.local   # ajuste VITE_KEYCLOAK_URL e VITE_API_BASE_URL
npm install
npm run dev
```

Valores típicos para `.env.local`:

```
VITE_KEYCLOAK_URL=http://localhost:8081
VITE_KEYCLOAK_REALM=segueme
VITE_KEYCLOAK_CLIENT_ID=segueme-frontend
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Acesse <http://localhost:5173>.

## Testes

```bash
# Backend: unitários + integração (Testcontainers sobe um PostgreSQL real)
cd backend && ./mvnw verify

# Frontend: lint, tipos e build
cd frontend && npm run lint && npm run build

# E2E: browser → frontend → backend → PostgreSQL, com login real no Keycloak
cd frontend && npm run test:e2e
```

Pré-requisitos do E2E (override do Keycloak, browser do Playwright) estão em
[`frontend/e2e/README.md`](frontend/e2e/README.md).

## Observabilidade

```bash
cd backend
docker compose -f docker-compose.yml -f docker-compose.observabilidade.yml up -d
```

Grafana em <http://localhost:3000> (`admin`/`admin`) com o dashboard **Segue-me · Backend** já
provisionado; Prometheus em <http://localhost:9090>. Detalhes em
[`backend/observabilidade/README.md`](backend/observabilidade/README.md).

## Perfis de acesso

`NACIONAL`, `DIOCESE`, `SETORIAL`, `PAROQUIA`, `EQUIPE_FICHAS`, `EQUIPE_MONTAGEM`,
`EQUIPE_COMANDO`, `EQUIPE_FINANCAS`, `POS_ENCONTRO` e `SEGUIDOR` — mapeados como roles
`ROLE_*` no Keycloak. Cada usuário só enxerga os dados do seu nível na hierarquia territorial.

## Documentação

- [`docs/00-indice.md`](docs/00-indice.md) — índice da documentação funcional (regras de negócio,
  requisitos, casos de uso, entidades)
- [`plano-implementacao.md`](plano-implementacao.md) — fases de implementação e Definition of Done
- [`skills/`](skills/) — padrões técnicos seguidos no código

## Convenções

- Branches `main` / `develop` / `feature/*` / `bugfix/*` / `hotfix/*`
- [Conventional Commits](https://www.conventionalcommits.org/pt-br/)
- Sem Lombok; um use case por ação do sistema; DTOs nunca expõem entidades JPA
