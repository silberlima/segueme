# Testes E2E de browser

Playwright dirigindo o stack real: browser → frontend (vite) → backend Spring → PostgreSQL, com
login de verdade no Keycloak. Nenhuma requisição é mockada.

## Pré-requisitos

1. **Infraestrutura** (PostgreSQL + Keycloak):

```bash
cd backend && docker compose -f docker-compose.yml -f docker-compose.e2e.yml up -d postgres keycloak
```

O override de E2E sobe o Keycloak com `KC_HOSTNAME=localhost` — assim browser, backend e Keycloak
concordam sobre o `iss` do token sem precisar de entrada no `/etc/hosts`.

2. **Backend** no host, na porta 8080 (os defaults do perfil `dev` já apontam para o PostgreSQL e o
   Keycloak locais):

```bash
cd backend && ./mvnw spring-boot:run
```

3. **Browser** do Playwright (uma vez):

```bash
cd frontend && npx playwright install chromium
```

## Executar

```bash
cd frontend && npm run test:e2e
```

O frontend sobe sozinho (`webServer` do `playwright.config.ts`). Para depurar com a UI do Playwright:

```bash
cd frontend && npm run test:e2e:ui
```

## O que o setup faz

`e2e/support/global-setup.ts`, antes da suíte:

- espera Keycloak e `/actuator/health` do backend responderem;
- semeia no PostgreSQL uma paróquia e dois jovens acompanhados (situação `SEGUIMISTA`) com ids fixos,
  de forma idempotente — a massa entra como fixture porque `SEGUIMISTA` só é alcançável pela API
  percorrendo a Fase 5 inteira (pré-inscrição → ficha, que exige um Encontro com equipe dirigente e
  comando montadas), o que tornaria o E2E desta fase refém daquela;
- provisiona no Keycloak o usuário `e2e.posencontro@segueme.test` com os perfis `POS_ENCONTRO` e
  `PAROQUIA` e o atributo `paroquia_id` da paróquia semeada (o realm compartilhado não é alterado).

Cada teste limpa os encontros pós e as presenças da massa antes de rodar, então a suíte é repetível e
os testes não dependem da ordem. As specs da Fase 13 também apagam relatórios, preferências e
notificações do usuário de E2E — filtrando pelo `sub` dele, e não esvaziando as tabelas, para não
levar junto dado de outro usuário do banco de desenvolvimento.

## Cobertura

| Spec | Cobre |
| --- | --- |
| `presencas.spec.ts` | Agendar encontro pós, chamada em lote persistida (sobrevive a reload), recusa de duas datas iguais (RN-POS-01), pool restrito aos acompanhados |
| `frequencia.spec.ts` | Apuração sobre todo o pool incluindo quem só faltou, recorte por período (RF-059) |
| `prioridades.spec.ts` | Ranking por frequência, faixas Alta/Média/Baixa da régua default e a regra exibida (RN-POS-02/03) |
| `paineis.spec.ts` | Entrada no painel do perfil ativo, menu restrito a esse painel (RN-DASH-01), KPIs reais dos painéis Pós-Encontro e Paróquia (RF-053) |
| `sistema.spec.ts` | Relatório gerado e baixado de ponta a ponta (UC-22), falha registrada com motivo, notificação de conclusão e badge de não lida (RN-SISTEMA-01/02), toggle de preferência persistido barrando a notificação (RF-057) |
