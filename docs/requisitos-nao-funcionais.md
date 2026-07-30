# Requisitos Não Funcionais

Apenas requisitos percebidos diretamente no protótipo — nenhum item foi inventado.

## Usabilidade
- **RNF-01:** o sistema deve manter navegação consistente entre perfis: mesmo padrão de sidebar (seletor de perfil, menu, rodapé) e header (breadcrumb, busca, notificações, avatar) em todas as telas internas.
- **RNF-02:** toda lista de dados deve comunicar claramente seus 4 estados possíveis quando aplicável: carregando (skeleton), com dados, vazio (com orientação de próxima ação) e erro (com ação de nova tentativa) — padrão explicitamente modelado na tela de Lista de Pessoas e que deve ser replicado às demais listas do sistema.
- **RNF-03:** ações destrutivas ou sensíveis (recusar convite, recusar transferência, registrar substituição) devem exigir confirmação/justificativa textual antes de serem efetivadas.
- **RNF-04:** fluxos longos (criação de encontro) devem ser guiados por assistente em etapas (wizard) com possibilidade de voltar, revisar e editar antes de confirmar.
- **RNF-05:** avisos de regra de negócio relevantes (ex.: limite de vagas, pré-requisito de equipe, natureza das sugestões automáticas) devem ser exibidos em contexto, no momento da decisão do usuário, não apenas em documentação externa.

## Segurança e Auditoria
- **RNF-06:** o acesso aos dados deve ser sempre restrito ao escopo hierárquico do perfil ativo (paróquia, setor, diocese ou nacional).
- **RNF-07:** ações que alteram vínculo de pessoas (transferência, empréstimo, substituição) devem ser auditáveis: o protótipo modela isso como histórico nunca apagado e notificações automáticas às partes responsáveis (Montagem).
- **RNF-08:** recusas de convite devem obrigatoriamente registrar justificativa textual, criando rastro de decisão.

## Performance
- **RNF-09:** listas grandes (ex.: 248 pessoas) devem ser paginadas em vez de carregadas integralmente.
- **RNF-10:** o sistema deve exibir feedback de carregamento (skeleton) durante buscas assíncronas de dados, evitando tela em branco.

## Responsividade
- **RNF-11:** o protótipo foi desenhado como aplicação de densidade alta voltada a desktop (sidebar fixa, grids de 4 colunas nos dashboards); não há evidência no protótipo de um layout mobile dedicado — comportamento em telas pequenas não foi especificado (ver pendências).

## Acessibilidade
- Não há requisitos de acessibilidade (contraste mínimo, navegação por teclado, leitores de tela, textos alternativos) explicitamente demonstrados ou testáveis no protótipo estático. **Não inventar valores — este item deve ser levantado à parte com o Product Owner.**

## 6. Decisões da implementação (Fase 14) — sujeitas a validação do PO

### 6.1 Os quatro estados de lista num componente só (RNF-02)
`shared/ui/ListState` concentra carregando (skeleton), com dados, vazio (com orientação de próxima
ação) e erro (com nova tentativa). Nenhuma tela reimplementa esses blocos — o padrão que existia só
na Lista de Pessoas passou a valer para **todas** as listas, inclusive os nove painéis da Fase 12
(o `PanelState` de `domains/dashboards` foi removido em favor do componente compartilhado).

Duas consequências de padronizar: **toda** lista ganhou botão "Tentar novamente" no estado de erro,
que antes só existia em algumas; e telas de detalhe (perfil de pessoa, detalhe de encontro) passaram
a usar o mesmo componente para carregando/erro, ainda que não sejam listas — o estado é o mesmo e
não faz sentido ter dois desenhos para ele.

### 6.2 Confirmação e justificativa das ações sensíveis (RNF-03/RNF-08)
`shared/ui/ConfirmDialog` é o único caminho para recusar convite, recusar transferência, recusar
empréstimo e registrar substituição. Antes, a justificativa era um campo solto ao lado do botão e a
ação disparava no clique; agora o diálogo diz **o que exatamente vai acontecer** (RNF-05) e só
libera o confirmar com justificativa preenchida. O texto digitado nunca vaza de uma recusa para a
seguinte porque o corpo do diálogo só existe enquanto ele está aberto.

### 6.3 Escopo territorial nos endpoints que recebem id (RNF-06)
As consultas filtradas por escopo já estavam corretas, mas os endpoints que recebem `encontroId`,
`paroquiaId` ou `encontroPosId` pela requisição **não verificavam nada**: ter a role certa bastava
para ler convites, equipes, inscrições, financeiro, palestras e a agenda pós de qualquer paróquia do
país, trocando o id da URL. A checagem passou a ser declarativa no próprio `@PreAuthorize`
(`@escopoTerritorial.podeVerEncontro(#encontroId)`), sem regra de permissão escrita à mão em
Controller ou Use Case.

Duas decisões dentro dela: **id inexistente passa** pela autorização (quem responde 404 é o Use
Case — negar aqui viraria 403 e revelaria, pela diferença de resposta, quais ids existem fora do
escopo de quem perguntou); e **token sem a claim territorial do seu perfil é negado** (403), em vez
de propagar o erro de regra de negócio do resolver e virar 409.

**Ainda em aberto:** endpoints que agem sobre o id do registro-filho (aceitar/recusar convite por
`conviteId`, aprovar transferência por `transferenciaId`, pagar lançamento por `lancamentoId`)
continuam sem checagem territorial — todos exigem perfil de gestão, mas nenhum verifica se aquele
registro é do território de quem age. Fechar isso é uma projeção por entidade em cada repositório,
no mesmo formato de `Territorio`.

### 6.4 Notificações automáticas das movimentações de vínculo (RNF-07)
Só a recusa de convite notificava. Transferência (aprovada e recusada), empréstimo (recusado e com
candidatos enviados) e substituição passaram a publicar evento de domínio com listener
`AFTER_COMMIT`, no mesmo desenho da Fase 13 — e sempre por `PublicarNotificacaoUseCase`, para que a
preferência do destinatário (RF-057) continue respeitada num ponto único.

O destinatário é o `createdBy` do registro afetado (quem solicitou a transferência, a Montagem que
pediu o empréstimo, quem colocou na equipe a pessoa substituída). É a mesma limitação de identidade
já documentada na Fase 7: sem diretório de usuários por role, não há como resolver "a Equipe
Montagem" literalmente.

O histórico append-only foi revisado e **já estava correto** — `HistoricoEvento` não expõe setter
nem remoção, e transferência aprovada e substituição gravam os dois lados da mudança.

### 6.5 Cobertura e pipeline
Gate de 80% de instruções (JaCoCo) no `verify`, o mesmo goal que o CI roda; DTOs, interfaces de
repositório, configuração e mappers gerados ficam de fora porque cobri-los mediria o framework.
Workflow novo de frontend (lint, typecheck, build).

A suíte de integração **estava vermelha antes desta fase** (12 falhas no `HEAD` da Fase 13, nunca
executadas localmente por falta de Docker): `LazyInitializationException` em quatro listagens que
mapeiam para DTO fora da transação com `open-in-view` desligado, um `ObjectMapper` de teste sem o
módulo de datas, e uma asserção de contagem absoluta sobre um banco compartilhado entre classes de
IT. Corrigidas todas — as três primeiras no código de produção, via `@EntityGraph`/fetch join.

### 6.6 Observabilidade
Dashboard e alertas em `backend/observabilidade/`, sobre as métricas que o Actuator já expunha
desde a Fase 0 (ver README de lá). **Roteamento de notificação não configurado**: as regras avaliam
e aparecem no Prometheus, mas não há Alertmanager porque o canal (e-mail, Slack, plantão) ainda não
foi definido com o PO.

### 6.7 Acessibilidade e responsividade — deliberadamente fora
Nada foi implementado nesses dois itens, por decisão explícita do plano: o protótipo não os
especifica e inventar um padrão aqui criaria requisito de produto sem PO. Continuam como iniciativa
própria a ser aberta — ver as pendências abaixo.

## Pendências de requisitos não funcionais
- Requisitos de desempenho quantitativos (tempo de resposta, volume de usuários simultâneos) não especificados.
- Requisitos de disponibilidade/SLA não especificados.
- Requisitos de internacionalização (o sistema hoje é 100% em português do Brasil) — não há indicação de necessidade de outros idiomas.
- Requisitos de acessibilidade formal (WCAG ou equivalente) não especificados — recomenda-se validar com o Product Owner antes da implementação.
- Requisito de responsividade mobile/tablet não especificado — o protótipo assume desktop.
