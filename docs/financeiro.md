# Financeiro

## 1. Objetivo
Controlar receitas, despesas e campanhas de arrecadação de um Encontro.

## 2. Telas envolvidas

### 2.1 Financeiro — visão geral (`screen_financeiro`)
- **Objetivo:** consolidar a saúde financeira do encontro.
- **Quem acessa:** perfil Equipe Finanças (principal) e Paróquia (consulta, resumido no dashboard).
- **Conteúdo:** 4 KPIs (Saldo atual, Receitas, Despesas, Campanhas ativas), gráfico de barras receitas × despesas, lista de movimentações recentes, saldo por encontro.
- **Ações:** "Extrato" (exportação), "Lançamento" (novo registro).

### 2.2 Receitas (`screen_receitas`)
- **Objetivo:** listar lançamentos de receita por categoria.
- **Componentes:** tabela com categorias e totais.

### 2.3 Despesas (`screen_despesas`)
- **Objetivo:** listar lançamentos de despesa com controle de situação.
- **Componentes:** tabela com colunas de situação: **aprovada / pendente / paga**.
- **Regra de negócio (inferida do modelo de estados):** uma despesa passa por um fluxo de situação — é registrada, aprovada, e então paga; o protótipo não detalha quem aprova.

### 2.4 Campanhas (`screen_campanhas`)
- **Objetivo:** gerenciar iniciativas de arrecadação (ex.: festa junina, venda de doces, rifa solidária, almoço beneficente).
- **Componentes:** cards com nome da campanha, valor arrecadado, barra de progresso até a meta, status (ativa/encerrada).
- **Regra de negócio:** campanha encerrada mantém seu resultado final visível ("encerrada · meta atingida").

## 3. Fluxos

### Fluxo: Registrar lançamento financeiro
- **Objetivo:** registrar uma receita ou despesa do encontro.
- **Ator:** Equipe Finanças.
- **Cenário principal:** usuário clica "Lançamento" → informa tipo (receita/despesa), categoria, valor, data → salva → lançamento aparece na lista de movimentações e atualiza o saldo.
- **Fluxo alternativo (despesa):** despesa nasce com situação "pendente" → é aprovada → é paga (fluxo de 3 estados observado nos dados, sem tela de aprovação dedicada no protótipo).

### Fluxo: Acompanhar campanha de arrecadação
- **Objetivo:** monitorar o progresso de uma campanha até sua meta.
- **Cenário principal:** usuário abre Campanhas → visualiza barra de progresso de cada campanha ativa.

## 4. Regras de negócio
- **RN-FINANCEIRO-01:** cada lançamento financeiro pertence a um encontro específico.
- **RN-FINANCEIRO-02:** despesas têm um ciclo de situação: pendente → aprovada → paga.
- **RN-FINANCEIRO-03:** campanhas de arrecadação têm meta e valor atual; podem ser encerradas mantendo o resultado histórico.
- **RN-FINANCEIRO-04:** o saldo do encontro é a diferença entre receitas e despesas registradas.

## 5. Casos de uso
- **UC — Registrar lançamento:** Ator: Equipe Finanças. Fluxo principal: Financeiro → "Lançamento" → preencher dados → salvar. Pós-condição: saldo atualizado.
- **UC — Aprovar despesa:** Ator: não identificado explicitamente no protótipo (provável Finanças ou Paróquia). Fluxo principal: despesa pendente → aprovação → pagamento. **Pendente de validação.**

## 6. Regras pendentes de validação
- Quem tem permissão para aprovar uma despesa (mesma Equipe Finanças, ou um segundo aprovador como o pároco/Paróquia) não está especificado — nenhuma tela de aprovação de despesas foi encontrada no protótipo, apenas o estado "aprovada" nos dados.
- Campos exatos do formulário de "Lançamento" (categorias predefinidas vs. texto livre, forma de pagamento) não detalhados — a tela de lançamento em si não foi aberta no protótipo revisado, apenas referenciada pelo botão.
- Regra de vínculo entre uma Receita e uma Campanha (toda receita de campanha é automaticamente lançada, ou é lançamento manual?) não especificada.
- Moeda e formatação (Real brasileiro, R$) são as únicas observadas — não há suporte multi-moeda aparente, mas isso não foi um requisito explícito.
