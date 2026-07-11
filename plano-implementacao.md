# Plano de Implementação — Sistema Segue-me

Este plano traduz a documentação funcional (`docs/*.md`, extraída do protótipo `prototype/Segue-me.html`) em um roteiro de implementação técnica, seguindo os padrões definidos em `skills/*.md`. Cada fase entrega uma fatia vertical completa: **backend + frontend** de um conjunto coeso de telas/regras, nunca só um lado.

## Índice
0. [Estado atual do repositório](#0-estado-atual-do-repositório)
1. [Stack e convenções (resumo executivo de `skills/`)](#1-stack-e-convenções-resumo-executivo-de-skills)
2. [Como ler cada fase](#2-como-ler-cada-fase)
3. [Fase 0 — Fundação técnica](#fase-0--fundação-técnica)
4. [Fase 1 — Autenticação, Acesso e Shell](#fase-1--autenticação-acesso-e-shell)
5. [Fase 2 — Hierarquia Territorial](#fase-2--hierarquia-territorial)
6. [Fase 3 — Cadastro de Pessoas](#fase-3--cadastro-de-pessoas)
7. [Fase 4 — Encontros + Equipe Dirigente/Comando](#fase-4--encontros--equipe-dirigentecomando)
8. [Fase 5 — Inscrições](#fase-5--inscrições)
9. [Fase 6 — Montagem de Equipes](#fase-6--montagem-de-equipes)
10. [Fase 7 — Convites, Aceites, Recusas e Coordenador](#fase-7--convites-aceites-recusas-e-coordenador)
11. [Fase 8 — Transferências e Empréstimos](#fase-8--transferências-e-empréstimos)
12. [Fase 9 — Financeiro](#fase-9--financeiro)
13. [Fase 10 — Conteúdo do Encontro](#fase-10--conteúdo-do-encontro)
14. [Fase 11 — Pós-Encontro](#fase-11--pós-encontro)
15. [Fase 12 — Dashboards por Perfil](#fase-12--dashboards-por-perfil)
16. [Fase 13 — Sistema Transversal](#fase-13--sistema-transversal-relatórios-notificações-configurações)
17. [Fase 14 — Hardening e Não-Funcionais](#fase-14--hardening-e-não-funcionais)
18. [Pendências que bloqueiam modelagem, por fase](#18-pendências-que-bloqueiam-modelagem-por-fase)
19. [Sequenciamento e paralelização](#19-sequenciamento-e-paralelização)
20. [Definition of Done geral](#20-definition-of-done-geral)

---

## 0. Estado atual do repositório

- **`backend/`** — skeleton Spring Boot 4.1.0 gerado (Maven, `groupId com.silberlima`, `artifactId segueme`, Java 25), com `spring-boot-starter-data-jpa`, `security`, `webmvc` já no `pom.xml`. Nenhum código de domínio ainda. `application.yaml` só define o nome da aplicação.
- **`frontend/`** — pasta vazia. Projeto React a criar do zero.
- **`mobile/`** — pasta vazia. **Fora do escopo deste plano**: não há documentação funcional nem skill técnica cobrindo um app mobile; não inventar stack/escopo aqui — abrir plano próprio quando houver definição do produto.
- **`prototype/Segue-me.html`** — protótipo navegável de alta fidelidade (export de ferramenta de design, ~5MB, bundle React embutido). É a fonte visual/comportamental que originou `docs/`. Não deve ser copiado ou reaproveitado como código do produto — serve apenas de referência de UX durante a implementação do frontend.
- **`docs/`** — 20 arquivos de documentação funcional (RF-xxx, RN-xxx, UC-xxx, entidades, pendências). Fonte de verdade de regras de negócio para este plano.
- **`skills/`** — 13 arquivos de padrão técnico (arquitetura, backend, API, segurança, persistência, qualidade, convenções, git, testes, contrato de frontend, observabilidade, CI/CD, componentes React).

---

## 1. Stack e convenções (resumo executivo de `skills/`)

**Backend** (`skills/01` a `skills/06`, `skills/08`, `skills/09`, `skills/11`, `skills/12`):
- Java 21+, Spring Boot 4+, Spring Data JPA, PostgreSQL, Liquibase, Docker, Maven. **Sem Lombok** — getters/setters/construtores explícitos.
- Arquitetura DDD em 4 camadas: `api` (controllers/DTOs/validação) → `application` (use cases/orquestração) → `domain` (entidades, VOs, interfaces de repositório, regras de negócio) → `infrastructure` (implementação de repositórios, config, segurança, integrações). Dependências sempre apontam para dentro; nunca inversão.
- Um Use Case = uma ação do sistema. Proibido `Utils`/`Helper`/`Manager`/`Common`.
- REST em `/api/v1`, DTOs de request/response (nunca expor entidade JPA), paginação (`page`/`size`/`sort`), status HTTP padronizados, OpenAPI obrigatório em todo endpoint.
- Segurança: Spring Security + OAuth2 + JWT + **Keycloak**; login social (Google/Instagram); autorização por Roles (nunca checada direto no Controller); senha nunca em texto puro (BCrypt, delegado ao Keycloak).
- Persistência: entidades com `id/createdAt/updatedAt/createdBy/updatedBy/deleted` (auditoria automática + soft delete), evitar N+1, `@Transactional` só onde necessário.
- Qualidade: SOLID/Clean Code, métodos <30 linhas, `GlobalExceptionHandler`, Jakarta Validation (nunca validação manual em Controller), MapStruct para mapeamento.
- Testes: JUnit 5 + Mockito + Spring Boot Test + **Testcontainers** (nunca H2), cobertura mínima 80%, padrão AAA, nomes `shouldXWhenY`, mockar só Repository/clients externos.
- CI/CD: pipeline checkout→compile→testes unitários→testes integração→qualidade→build Docker→deploy; SemVer; ambientes dev/homologação/produção.
- Observabilidade: Actuator (`/health`, `/info`, `/metrics`, `/prometheus`) + Micrometer + Prometheus/Grafana; logs estruturados via SLF4J (nunca `System.out.println`).

**Frontend** (`skills/10`, `skills/padrao-componentes-react-tailwind.md`):
- React + TypeScript + Tailwind CSS, organizado por **domínio de negócio**, não por tipo técnico: `src/domains/<dominio>/{components,hooks,domain,services,screens}` + `src/shared/{ui,hooks,utils,types}` + `src/app/{routes,providers}`.
- Um componente = uma pasta (`Componente.tsx` + `Componente.types.ts` + `index.ts` + hook opcional). Tipos de domínio nunca inline num `.tsx`. Sem CSS solto, sem `style={{}}` estático, sem `@apply`.
- Domínios não importam uns dos outros diretamente — só via `shared/`.
- Nomes de pastas de domínio já alinhados ao vocabulário funcional: `pessoas`, `encontros`, `equipes`, `montagem`, `inscricoes`, `financeiro`, `setorial`, `conteudo`, `sistema`.

**Git** (`skills/08`): branches `main`/`develop`/`feature/*`/`bugfix/*`/`hotfix/*`/`release/*`; Conventional Commits obrigatório; PR com objetivo/descrição/impactos/checklist/screenshots; squash merge; nunca commit direto na `main`.

---

## 2. Como ler cada fase

Toda fase abaixo segue o mesmo template: **Objetivo**, **Dependências**, **Backend**, **Frontend**, **Regras/Requisitos cobertos**, **Pendências a validar**, **Definition of Done**. RF/RN/UC referenciam `docs/requisitos-funcionais.md`, `docs/regras-de-negocio.md` e `docs/casos-de-uso.md`.

**Perfis ↔ Roles Keycloak** (mapeamento usado em todas as fases, RN-ACESSO-01/02):
`ROLE_NACIONAL`, `ROLE_DIOCESE`, `ROLE_SETORIAL`, `ROLE_PAROQUIA`, `ROLE_EQUIPE_FICHAS`, `ROLE_EQUIPE_MONTAGEM`, `ROLE_EQUIPE_COMANDO`, `ROLE_EQUIPE_FINANCAS`, `ROLE_POS_ENCONTRO`, `ROLE_SEGUIDOR`. O papel "Coordenador de equipe" (pendência #33) **não** entra nesta lista — ver Fase 7.

---

## Fase 0 — Fundação técnica

**Objetivo:** infraestrutura compartilhada que toda fase seguinte usa; nenhuma feature de negócio ainda.
**Dependências:** nenhuma.

### Backend
- Criar pacotes `com.silberlima.segueme.{api,application,domain,infrastructure}` como base de todo módulo futuro (cada domínio depois replica essas 4 camadas dentro de um subpacote, ex. `pessoas.domain`, `pessoas.application`).
- `application.yml` com profiles `dev`/`test`/`prod`; segredos só via variáveis de ambiente (nunca hardcoded).
- `Dockerfile` + `docker-compose.yml` subindo app + PostgreSQL + Keycloak.
- Liquibase: `db.changelog-master.xml` + convenção de changelog por domínio (`db/changelog/pessoas/`, `db/changelog/encontros/`, ...).
- Spring Security + OAuth2 Resource Server validando JWT emitido pelo Keycloak; realm `segueme` com as 10 roles listadas na seção 2; JWT contendo `id`/`email`/`roles`.
- `GlobalExceptionHandler` + hierarquia base de exceções de domínio (nunca stack trace na resposta).
- Entidade base auditável (`AbstractAuditableEntity`: id/createdAt/updatedAt/createdBy/updatedBy/deleted) com auditoria JPA automática e soft delete.
- OpenAPI/Swagger habilitado.
- Actuator: `/health`, `/info`, `/metrics`, `/prometheus`.
- Pipeline CI mínimo (`skills/12`) rodando mesmo sem features (compile + testes vazios).

### Frontend
- Inicializar projeto React + TypeScript + Tailwind em `frontend/` (Vite).
- Estrutura de pastas conforme `skills/padrao-componentes-react-tailwind.md`: `src/domains/`, `src/shared/`, `src/app/`.
- `app/providers`: `AuthProvider` (OIDC contra Keycloak), `QueryClientProvider` (React Query) para todos os `*.service.ts`.
- `shared/ui` inicial: `Button`, `Badge`, `Modal`, `Table`, `Input`, `Select`, `Tabs`, `Drawer`, `Skeleton` (base dos 4 estados de lista — RNF-02).
- `app/routes`: roteador com guarda de rota por role.
- Cliente HTTP tipado em `shared/` com injeção automática do JWT.

**DoD:** `docker-compose up` sobe app + Postgres + Keycloak; endpoint protegido de teste retorna 401 sem token e 200 com token válido; `mvn test` e `npm test` passam (mesmo vazios); pipeline CI verde; shell React renderiza um layout vazio autenticado.

---

## Fase 1 — Autenticação, Acesso e Shell

**Objetivo:** login, troca de perfil, navegação (sidebar/header/busca/notificações), mapa de navegação.
**Dependências:** Fase 0.
**Docs:** `autenticacao-acesso.md` · **Cobertura:** RF-001 a RF-006, RN-ACESSO-01/02/03, UC-01, UC-02, UC-21.

### Backend
- `GET /api/v1/me` — perfil autenticado + perfis permitidos (RN-ACESSO-02) a partir das roles do JWT.
- Login/senha/recuperação de senha ficam delegados ao Keycloak (não reimplementar fluxo de senha no backend da aplicação).
- `GET /api/v1/search?q=` — busca global de pessoas e encontros, escopada pelo perfil ativo (RN-ACESSO-01/RN-04, aplicado via filtro de escopo territorial reutilizável entre domínios).
- Entidade `Notificacao` (RN-SISTEMA-01/02): `GET /api/v1/notificacoes`, `PATCH /api/v1/notificacoes/marcar-lidas`.

### Frontend
- `domains/sistema`: `LoginScreen`, `RecuperarSenhaScreen`, `NavigationMapScreen` (catálogo de telas com tags "Pronto para navegar"/"Previsto no escopo").
- `shared/ui`: `Sidebar` (seletor de perfil, menu dinâmico por perfil ativo, badges de pendência), `Header` (breadcrumb, busca global `⌘K`, sino de notificações, avatar).
- `app/providers/ProfileContext` — perfil ativo trocável, redefine menu + dashboard inicial (RN-ACESSO-02).

**Pendências a validar antes de travar o design (`pendencias-gerais.md` #1–4, #28):** regras de validação de login/senha, fluxo completo de redefinição de senha, regra de atribuição de perfil(is) por usuário real (hoje o protótipo troca livremente — decidir se um usuário real tem 1 ou N perfis), mensagens de erro/sucesso de login.

**DoD:** login real via Keycloak; troca de perfil atualiza menu e painel inicial; busca global retornando pessoas/encontros do escopo; drawer de notificações funcional; mapa de navegação lista todas as telas do sistema.

---

## Fase 2 — Hierarquia Territorial

**Objetivo:** estrutura Nacional → Diocese → Setor → Paróquia, base de escopo de todo o sistema.
**Dependências:** Fase 1 (perfil/escopo).
**Docs:** `hierarquia-territorial.md` · **Cobertura:** RF-054, RF-055, RN-HIERARQUIA-01/02/03, UC-19.

### Backend
- Entidades `Diocese`, `Setor`, `Paroquia` com hierarquia estrita (RN-HIERARQUIA-01/03).
- Serviço/aspecto de escopo territorial reutilizável (usado por praticamente todo domínio futuro) implementando RN-HIERARQUIA-02/RN-04.
- `GET /api/v1/dioceses` (tabela hierárquica com métricas agregadas), `GET /api/v1/paroquias` (escopado ao setor/diocese do usuário).

### Frontend
- `domains/hierarquia`: `DiocesesScreen` (tabela Nacional/Diocese/Setor/Paróquia), `ParoquiasScreen` (cards com estatísticas e status do encontro atual).

**Pendência (`pendencias-gerais.md` #25):** cadastro/edição de Diocese/Setor/Paróquia não existe no protótipo (só consulta) — validar com o PO se entra no MVP como CRUD administrativo ou é seed/migration.

**DoD:** consulta hierárquica funcional e corretamente escopada por perfil.

---

## Fase 3 — Cadastro de Pessoas

**Objetivo:** cadastro nacional único de pessoa, com histórico, casais e competências — entidade mais reutilizada do sistema.
**Dependências:** Fase 2 (Paróquia).
**Docs:** `cadastro-de-pessoas.md` · **Cobertura:** RF-007 a RF-013, RN-PESSOA-01 a 05, RN-01/02/03, UC-03, UC-04.

### Backend
- Entidade `Pessoa` (situação: `PRE_INSCRITO → SEGUIMISTA → SEGUIDOR`, mais `INSCRITO`; dados de saúde, contato de emergência, competências, paróquia atual).
- Entidade `Casal` (2 pessoas vinculadas — RN-03).
- Entidade `Competencia` (catálogo nacional, seed via Liquibase).
- Entidade `HistoricoEvento` **append-only** (RN-02: nunca update/delete, só insert).
- Endpoints: CRUD de criação/consulta de pessoa (paginado, com busca por nome/paróquia/competência), casais, competências, histórico por pessoa. Pessoa nasce com situação `PRE_INSCRITO` (RN-PESSOA-04).

### Frontend
- `domains/pessoas`: `PersonListScreen` (os 4 estados — carregando/dados/vazio/erro, RNF-02, com skeleton por linha), `PersonFormScreen` (4 seções), `PersonProfileScreen`, `PersonHistoryScreen` (linha do tempo), `CouplesScreen`, `CompetenciesScreen`.

**Pendências (`pendencias-gerais.md` #17, #18, #16, #23, #30):** validação exata de campos (telefone, data), deduplicação de cadastro, lista definitiva das 8 competências, edição de pessoa já existente (não modelada — decidir se entra no MVP), quais perfis podem criar/editar (vs. só visualizar).

**DoD:** pessoa criável e listável com os 4 estados de UI; perfil completo e histórico navegáveis; competências semeadas e disponíveis para seleção.

---

## Fase 4 — Encontros + Equipe Dirigente/Comando

**Objetivo:** criação e gestão do Encontro, o objeto central de operação de cada paróquia.
**Dependências:** Fase 3 (Pessoa, para Equipe Dirigente/Comando).
**Docs:** `gerenciamento-de-encontros.md`, `equipe-dirigente-e-comando.md` · **Cobertura:** RF-014 a RF-021, RN-ENCONTRO-01 a 05, RN-DIRIGENTE-01, RN-COMANDO-01, UC-05.

> **Bloqueio a resolver antes de codar o modelo de dados:** nomenclatura divergente das 5 pastas da Equipe Dirigente (`pendencias-gerais.md` #38) — wizard usa "Finanças/Fichas/Palestras/Pós-Encontro/Montagem", tela de consulta usa "Coordenação Geral/Formação/Liturgia/Secretaria/Logística". Validar com o PO antes de definir o enum; não assumir silenciosamente uma das duas.

### Backend
- Entidade `Encontro` (número sequencial por paróquia — RN-ENCONTRO-01, status/fase, limite de vagas).
- Entidade `EquipeDirigente` (5 pastas fixas, 2 jovens + 1 casal cada — RN-DIRIGENTE-01/RN-ENCONTRO-03), `EquipeComando` (coordenador geral + casal).
- `CreateEncontroUseCase` orquestrando os 5 passos do wizard num único fluxo transacional; permite pasta incompleta sem bloquear (RN-ENCONTRO-05); ao concluir, entrega o encontro à Equipe Comando (RN-ENCONTRO-04).
- Endpoints de consulta (`GET /api/v1/encontros`, `GET /api/v1/encontros/{id}`).

### Frontend
- `domains/encontros`: `EncounterListScreen`, `CreateEncounterWizard` (stepper de 5 passos client-side com voltar/revisar/editar — RNF-04), `EncounterDetailScreen`, `DirigenteScreen`, `ComandoTeamScreen`.

**Pendências (`pendencias-gerais.md` #5, #9, #24, #26):** múltiplos encontros ativos simultâneos por paróquia, etapa de aprovação em nível de Diocese/Setorial (mencionada no dashboard da Diocese mas não modelada no wizard), edição/cancelamento de encontro já criado, conteúdo completo de "Detalhes do Encontro" além da aba "equipes".

**DoD:** encontro criado via wizard com persistência real (diferente do protótipo, que não persistia), gerando Equipe Dirigente e Comando.

---

## Fase 5 — Inscrições

**Objetivo:** funil de inscrição de participantes, do primeiro contato até virar Seguimista.
**Dependências:** Fase 4 (Encontro), Fase 3 (Pessoa).
**Docs:** `inscricoes.md` · **Cobertura:** RF-022 a RF-027, RN-INSCRICAO-01 a 05, RN-14/15/16, UC-06, UC-07.

### Backend
- Entidades `PreInscricao`, `FichaInscricao` (4 seções), `CadastroReserva` (fila FIFO).
- `PreInscreverUseCase`, `PreencherFichaUseCase` (exclusivo perfil `ROLE_EQUIPE_FICHAS` — RN-INSCRICAO-01; ao confirmar, muda `Pessoa.situacao` para `SEGUIMISTA` — RN-INSCRICAO-02), `PromoverReservaUseCase`.
- Regra de transbordo automático: ao atingir `Encontro.limiteVagas`, nova pré-inscrição vai para `CadastroReserva` (RN-INSCRICAO-03/RN-16), ordem de chegada, prioridade automática no próximo encontro da mesma paróquia (RN-INSCRICAO-04).

### Frontend
- `domains/inscricoes`: `PreEnrollmentScreen` (abas por status + indicador de ocupação de vagas), `EnrollmentFormScreen` (ficha, 4 seções + stepper de status do candidato), `WaitlistScreen`, `ParticipantsScreen` (funil visual completo).

**Pendências (`pendencias-gerais.md` #19, #17, #13-parcial):** campos mínimos de "Nova pré-inscrição" (tela não desenhada no protótipo — precisa de definição de UX antes do form), critério exato de "vaga se abre" para promoção da reserva, regras de pagamento (só coluna citada, sem formulário).

**DoD:** fluxo Pré-inscrição → Ficha → Confirmação → Seguimista funcionando ponta a ponta, com transbordo automático para reserva.

---

## Fase 6 — Montagem de Equipes

**Objetivo:** composição das equipes de serviço do encontro, com sugestões automáticas de candidatos.
**Dependências:** Fase 4 (Encontro), Fase 3 (Pessoa/Competência). Fator "frequência no pós-encontro" do score pode nascer com peso 0/placeholder até a Fase 11 entregar dado real (ver seção 19).
**Docs:** `montagem-de-equipes.md` · **Cobertura:** RF-028 a RF-033, RN-MONTAGEM-01 a 07, RN-06 a 10, UC-08, UC-09.

### Backend
- Entidade `Equipe` (catálogo nacional de tipos — nome/min/max/pré-requisito, seed; a paróquia só preenche vagas, nunca cria/renomeia — RN-06/RN-MONTAGEM-07) e integrantes com status (aceito/pendente/recusado).
- `SuggestionEngine` como **domain service puro** (sem dependência de framework) implementando o score 0–100 com pesos fixos 35/30/20/15% (histórico em equipes semelhantes / competências / frequência pós-encontro / pré-requisito — RN-09/RN-MONTAGEM-03). A fórmula interna de cada fator é pendência (`pendencias-gerais.md` #6) — implementar com melhor esforço documentado em comentário/ADR e expor via versão de endpoint para permitir recalibração sem quebrar contrato.
- Entidade `Substituicao` (justificativa obrigatória — RN-MONTAGEM-05, preserva histórico de ambas as pessoas — RN-MONTAGEM-06/RN-02/13).
- `@PreAuthorize("hasRole('EQUIPE_MONTAGEM')")` em todos os endpoints de edição/sugestão (RN-MONTAGEM-01/02/08) — nunca checagem manual no Controller (`skills/04`).

### Frontend
- `domains/montagem`: `TeamsWorkspaceScreen` (lista + detalhe; ações de edição só renderizadas para o perfil Montagem), `SuggestionsScreen` (tela cheia, exclusiva Montagem, com selo "Visível só para a Montagem" e aviso "as sugestões nunca substituem a decisão da Montagem" — RN-MONTAGEM-04/10), `SubstitutionsScreen`.

**Pendências (`pendencias-gerais.md` #6, #7):** fórmula exata de cada fator do score; se "Confirmar montagem" bloqueia ou não com equipes incompletas.

**DoD:** Montagem monta equipe usando sugestões ranqueadas e registra substituição com justificativa; demais perfis veem a equipe em modo somente leitura, sem sugestões.

---

## Fase 7 — Convites, Aceites, Recusas e Coordenador

**Objetivo:** ciclo de convite (Montagem → Comando → Coordenador → Integrantes) e tratamento de recusas.
**Dependências:** Fase 6 (Equipe).
**Docs:** `convites-aceites-recusas.md` · **Cobertura:** RF-034 a RF-039, RN-CONVITE-01 a 04, RN-11/12/13, UC-10, UC-11, UC-12.

### Backend
- Entidade `Convite` (pessoa, equipe, papel, status, justificativa obrigatória em recusa via Jakarta Validation).
- Máquina de estados no domínio garantindo a ordem RN-CONVITE-01 (Montagem monta → Comando convida coordenador → coordenador aceita → coordenador convida integrantes).
- Evento de domínio `ConviteRecusadoEvent` (Spring Application Events) disparando notificação imediata à Equipe Montagem (RN-12/RF-038), tratada como pendência de substituição (liga com Fase 6).

### Frontend
- `domains/equipes`: `InvitesScreen`, `AcceptancesScreen`, `RefusalsScreen` (com justificativa visível), `TeamDetailScreen`, `CoordinatorPanelScreen` (convidar integrante, reenviar convite pendente).

**Pendência de modelagem (`pendencias-gerais.md` #33):** "Coordenador de equipe" **não** é um dos 10 perfis trocáveis do protótipo. Recomendação: modelar como papel condicionado a um vínculo (`pessoa_id + equipe_id`, não uma role global) — a pessoa acessa o Painel do Coordenador só para a(s) equipe(s) onde tem convite aceito como coordenador, não como um "11º perfil" na lista de troca.

**Outras pendências (`pendencias-gerais.md` #8):** prazo/expiração de convite pendente e regra de reenvio (quantas vezes, reinicia prazo?).

**DoD:** cadeia de convite completa até a recusa gerar notificação e refletir como pendência no painel de Montagem.

---

## Fase 8 — Transferências e Empréstimos

**Objetivo:** movimentação de pessoas entre paróquias (definitiva ou temporária), sempre mediada pelo Setorial.
**Dependências:** Fase 3 (Pessoa), Fase 6 (Equipe).
**Docs:** `transferencias-e-emprestimos.md` · **Cobertura:** RF-040 a RF-046, RN-MOVIMENTO-01 a 05, RN-17/18, UC-13 a UC-16.

### Backend
- Entidades `Transferencia` e `Emprestimo`; `Solicitacao` como agregação/view das duas (inbox único do Setorial), não necessariamente tabela própria.
- Fluxo bidirecional Empréstimo como use cases separados: `RequestLoanUseCase` (Montagem), `SelectAndSendCandidatesUseCase` (Setorial, filtros dinâmicos por paróquia/habilidade — RN-MOVIMENTO-05), `EffectivateLoanUseCase` (Montagem, RN-18).
- Aprovação/recusa de transferência restrita a `ROLE_SETORIAL` (RN-17/RN-MOVIMENTO-01).
- Histórico da pessoa preservado em ambos os fluxos (RN-MOVIMENTO-03/RN-02).

### Frontend
- `domains/setorial`: `TransfersScreen`, `LoansScreen` (dois componentes distintos para a visão Montagem e a visão Setorial — mesma tela, contextos diferentes), `RequestsInboxScreen` (cards com Autorizar/Aprovar e Recusar).

**Pendências (`pendencias-gerais.md` #9, #10, #11):** quem dispara o gatilho de uma Transferência (pessoa, origem ou destino), prazo de vigência do Empréstimo (encerra automaticamente ao fim do encontro?), limite de empréstimos simultâneos por paróquia — sinalizar como TODO documentado no código, não decidir silenciosamente.

**DoD:** Setorial aprova/recusa via inbox único; empréstimo efetivado reflete no roster da equipe em Montagem, mantendo o vínculo de origem da pessoa.

---

## Fase 9 — Financeiro

**Objetivo:** controle de receitas, despesas e campanhas de arrecadação de um encontro.
**Dependências:** Fase 4 (Encontro).
**Docs:** `financeiro.md` · **Cobertura:** RF-047 a RF-050, RN-FINANCEIRO-01 a 04, UC-17.

### Backend
- Entidade `LancamentoFinanceiro` (receita/despesa, categoria, ciclo de situação de despesa `PENDENTE → APROVADA → PAGA` — RN-FINANCEIRO-02), `Campanha` (meta, valor arrecadado, status ativa/encerrada).
- Saldo do encontro = receitas − despesas (RN-FINANCEIRO-04), calculado, não persistido redundantemente.
- Aprovador de despesa é pendência (`pendencias-gerais.md` #12) — modelar campo `aprovadoPor` genérico aceitando qualquer usuário com `ROLE_EQUIPE_FINANCAS` ou `ROLE_PAROQUIA` até validação do PO.

### Frontend
- `domains/financeiro`: `FinanceOverviewScreen` (KPIs + gráfico receitas×despesas), `RevenuesScreen`, `ExpensesScreen` (coluna de situação), `CampaignsScreen` (barra de progresso), formulário de "Lançamento" (tela não aberta no protótipo — desenhar campos mínimos: tipo, categoria, valor, data).

**Pendência (`pendencias-gerais.md` #13):** categorias predefinidas vs. texto livre, forma de pagamento — validar antes de fechar o formulário de lançamento.

**DoD:** lançamento cria movimentação e recalcula saldo/gráfico em tempo real; campanha mostra progresso até a meta.

---

## Fase 10 — Conteúdo do Encontro

**Objetivo:** palestras oficiais e palestrantes designados.
**Dependências:** Fase 4 (Encontro), Fase 3 (Pessoa, quando palestrante é Seguidor).
**Docs:** `conteudo-palestras.md` · **Cobertura:** RF-051, RF-052, RN-CONTEUDO-01/02, UC-18.

### Backend
- Entidade `Palestra` (7 temas fixos e numerados, ordem imutável, não reordenável pela paróquia — RN-22/RN-CONTEUDO-01, seed nacional).
- Entidade `Palestrante` (tipo enum `EXTERNO`/`SEGUIDOR`/`CLERO` — só `SEGUIDOR` referencia `Pessoa`, RN-CONTEUDO-02/RN-23).

### Frontend
- `domains/conteudo`: `LecturesScreen` (lista em ordem fixa), `SpeakersScreen` (grid, com fluxo de cadastro de convidado externo sem gerar `Pessoa`).

**Pendências (`pendencias-gerais.md` #15, #27, #31):** lista definitiva e ementa dos 7 temas oficiais (seed placeholder documentado até confirmação do PO), fluxo de cadastro do Convidado externo (campos mínimos), quem tem permissão para editar a designação de palestrante.

**DoD:** as 7 palestras aparecem em ordem fixa; palestrante designável nos 3 tipos.

---

## Fase 11 — Pós-Encontro

**Objetivo:** acompanhar frequência dos jovens após o encontro e priorizá-los para as próximas equipes.
**Dependências:** Fase 3 (Pessoa), Fase 2 (Paróquia).
**Docs:** `pos-encontro.md` · **Cobertura:** RF-058, RF-059, RN-POS-01 a 03, RN-19, UC-20.

> **Nota de escopo:** as telas de detalhe (Presenças, Frequência, Prioridades) **não estão desenhadas** no protótipo — só referenciadas no menu (`pendencias-gerais.md` #20). Esta fase inclui uma sub-etapa de **design de UX antes do desenvolvimento** dessas 3 telas; o painel principal (`dash_posencontro`) já está coberto e pode ser implementado imediatamente.

### Backend
- Entidade `FrequenciaPosEncontro` (pessoa, data do encontro pós, presença — RN-POS-01).
- Cálculo de prioridade (Alta/Média/Baixa por proporção de presenças — RN-POS-03) implementado como regra **parametrizável** (régua de corte configurável), já que a régua exata é pendência (`pendencias-gerais.md` #14).
- Este dado alimenta o fator "frequência no pós-encontro" (peso 20%) do `SuggestionEngine` da Fase 6 — ao concluir esta fase, revisitar a Fase 6 para trocar o placeholder pelo cálculo real.

### Frontend
- `domains/posencontro`: painel já coberto na Fase 1 shell (dashboard); telas novas `AttendanceScreen`, `FrequencyScreen`, `PriorityScreen` — a desenhar antes de codar.

**DoD:** presença registrável; ranking de prioridade calculado e consumido pela Fase 6.

---

## Fase 12 — Dashboards por Perfil

**Objetivo:** os 9 painéis iniciais, um por perfil operacional.
**Dependências:** todas as fases de dados (4 a 11) — é fundamentalmente uma fase de **agregação**.
**Docs:** `dashboards-por-perfil.md` · **Cobertura:** RF-053, RN-DASH-01/02.

> **Recomendação de execução:** não deixar 100% para o fim. Implementar cada painel logo após o módulo de dados correspondente ficar pronto (ex.: `dash_montagem` ao final da Fase 6, `dash_financas` ao final da Fase 9). Esta fase é o ponto de consolidação final e de padronização visual entre os 9 painéis.

### Backend
- Endpoints de agregação por perfil (ex. `GET /api/v1/dashboards/nacional`), como queries/views otimizadas — evitar N+1 (`skills/05`), nunca `SELECT *`.

### Frontend
- 9 telas de dashboard reaproveitando os mesmos componentes `shared/ui` (KPI card, gráfico de barras simples, lista "top N + ver todos").

**Pendência (`pendencias-gerais.md`, seção Dashboards):** regras de cálculo exatas dos KPIs (período de apuração, corte de data) são ilustrativas no protótipo — validar com o PO por dashboard.

**DoD:** os 9 painéis carregam dados reais (não mais estáticos) dos módulos já implementados.

---

## Fase 13 — Sistema Transversal: Relatórios, Notificações, Configurações

**Objetivo:** funcionalidades cross-cutting que tocam todos os perfis.
**Dependências:** Fase 1 (notificações básicas já existem desde ali); esta fase completa o módulo.
**Docs:** `sistema-relatorios-config.md` · **Cobertura:** RF-056, RF-057, RN-SISTEMA-01/02, UC-22.

### Backend
- Entidade `Relatorio` (tipo, data de geração); geração como **processamento assíncrono** (`skills/05`: operações demoradas nunca bloqueiam a request/transação).
- Endpoint de preferências de notificação por usuário.

### Frontend
- `domains/sistema`: `ReportsScreen` (tipos + "gerados recentemente"), `NotificationsFullScreen` (versão tela cheia do drawer), `SettingsScreen` (conta, notificações, perfis de acesso).

**Pendências (`pendencias-gerais.md` #15-sistema, #34, #37):** tipos exatos dos 6 relatórios e formato de saída (PDF/planilha), quem pode alterar notificações de outros usuários (individual vs. administração global).

**DoD:** relatório gerável de ponta a ponta (mesmo que MVP gere um formato simples); preferências de notificação persistidas por usuário.

---

## Fase 14 — Hardening e Não-Funcionais

**Objetivo:** consolidar os requisitos não funcionais (`requisitos-nao-funcionais.md`) que atravessam todas as fases anteriores.
**Dependências:** todas.

- **RNF-02** — padronizar os 4 estados (carregando/dados/vazio/erro) em **todas** as listas do sistema via um único componente `shared/ui/ListState`, não reimplementado por domínio.
- **RNF-03** — confirmação/justificativa obrigatória em toda ação destrutiva/sensível (recusar convite, recusar transferência, registrar substituição) via `shared/ui/ConfirmDialog` reutilizado.
- **RNF-06/07/08** — revisão ponta a ponta de auditoria, histórico append-only e notificações automáticas.
- **Cobertura de testes** — atingir e sustentar ≥80% (`skills/09`) retroativamente nas fases anteriores; pipeline CI/CD completo (`skills/12`) validando todo PR.
- **Observabilidade** — dashboards Grafana e alertas sobre as métricas já expostas desde a Fase 0.
- **Acessibilidade e responsividade mobile/tablet** — explicitamente **não cobertas** pelo protótipo original (`requisitos-nao-funcionais.md`, seção Acessibilidade/Responsividade). Não inventar padrão aqui: abrir como iniciativa própria e validar com o PO antes de comprometer prazo de release com isso.

---

## 18. Pendências que bloqueiam modelagem, por fase

| Pendência (`pendencias-gerais.md`) | Fase afetada | Ação recomendada |
|---|---|---|
| #38 Nomenclatura das 5 pastas da Equipe Dirigente | Fase 4 | **Resolver antes de codar o enum** — validar com PO |
| #33 "Coordenador" não é um dos 10 perfis | Fase 7 | Modelar como papel escopado a vínculo pessoa+equipe, não role global |
| #3 Atribuição de perfil(is) por usuário real | Fase 1 | Decidir 1:1 ou 1:N perfil por usuário antes do modelo de `Usuario` |
| #6 Fórmula exata dos 4 fatores do score | Fase 6 | Implementar com melhor esforço documentado, versionado |
| #14 Régua de corte Alta/Média/Baixa (pós-encontro) | Fase 11 | Implementar parametrizável, não hardcoded |
| #9, #10, #11 Gatilho de transferência, prazo e limite de empréstimo | Fase 8 | TODO documentado no código, não decidir silenciosamente |
| #12 Aprovador de despesa | Fase 9 | Campo genérico até validação do PO |
| #15/#16 Lista definitiva de temas e competências | Fases 3, 10 | Seed placeholder documentado |
| #20 Telas de detalhe do Pós-Encontro não desenhadas | Fase 11 | Sub-etapa de design de UX antes do dev |
| #19 Formulário de "Nova pré-inscrição" não desenhado | Fase 5 | Idem |
| #25 Cadastro de Diocese/Setor/Paróquia | Fase 2 | Confirmar se é CRUD de produto ou seed administrativo |

Lista completa (41 itens) em `docs/pendencias-gerais.md` — as acima são as que **bloqueiam decisão de modelagem de dados**, não apenas UX.

---

## 19. Sequenciamento e paralelização

- **Fases 0–3 são sequenciais e bloqueantes** (fundação, acesso, hierarquia, pessoas) — todo o resto depende delas.
- A partir da **Fase 4**, backend e frontend de uma mesma fase podem ser paralelizados entre duplas, mas a ordem **entre** fases 4→11 respeita dependência de dados: Encontro (4) precede Inscrições (5), Montagem (6), Financeiro (9) e Conteúdo (10).
- **Montagem (6) e Pós-Encontro (11)** têm dependência circular leve: o score de sugestão usa frequência pós-encontro (peso 20%), mas pós-encontro só existe depois que já houve encontros e equipes. Solução: implementar Montagem na Fase 6 com esse fator zerado/placeholder documentado, e revisitar ao concluir a Fase 11.
- **Dashboards (12) e Sistema (13)** são bons candidatos a fatiar ao longo do projeto (um dashboard por módulo pronto) em vez de empilhar tudo no fim — evita uma fase gigante de agregação no final.
- **Fase 14 (hardening)** roda em paralelo desde o início como critério de aceite de cada PR (cobertura de teste, CI, RNF-02/03), não como uma fase isolada só ao final.

---

## 20. Definition of Done geral

Aplica-se a **toda** fase, além do DoD específico listado nela:

**Backend**
- Use cases com teste unitário + teste de integração via Testcontainers.
- Endpoints documentados no OpenAPI.
- Nenhuma regra de negócio em Controller; nenhuma checagem de permissão manual fora de `@PreAuthorize`/config de segurança.
- DTOs nunca expõem entidade JPA; mapeamento via MapStruct.
- Migrations Liquibase versionadas, nunca alteradas retroativamente.

**Frontend**
- Componentes seguindo `skills/padrao-componentes-react-tailwind.md` (pasta própria, tipos isolados em `.types.ts`, sem lógica de negócio em `.tsx`).
- Tela testada manualmente cobrindo golden path + estados de erro/vazio quando aplicável (RNF-02).
- Sem import cruzado direto entre `domains/*` diferentes.

**Git**
- Branch `feature/<dominio>-<funcionalidade>`, Conventional Commits.
- PR com objetivo, descrição, impactos, checklist e screenshots (frontend).
- PR referencia os RF-xxx/RN-xxx/UC-xxx implementados nesta fase, para rastreabilidade com `docs/`.
