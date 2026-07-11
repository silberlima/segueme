# Gerenciamento de Encontros

## 1. Objetivo
Permitir a criação, consulta e acompanhamento dos Encontros — o evento central da operação de cada paróquia.

## 2. Telas envolvidas

### 2.1 Lista de Encontros (`screen_encontros`)
- **Objetivo:** listar todos os encontros de uma paróquia (ou, em perfis superiores, do escopo territorial).
- **Campos exibidos por item:** número, título (nº + padroeiro), status (ativo/encerrado), datas, local, paróquia, contagem de seguidores/equipes/equipistas, coordenador geral, fase atual (ex.: "Montagem de equipes", "Inscrições abertas", "Encerrado").
- **Ações:** abrir Detalhes do Encontro; iniciar novo encontro (via wizard, a partir do dashboard da Paróquia).

### 2.2 Wizard "Criar Encontro" (`screen_criar_encontro`)
- **Objetivo:** guiar a criação de um novo encontro em 5 passos.
- **Quem acessa:** perfil Paróquia (botão "Novo encontro" no painel da Paróquia).
- **Estrutura:** stepper de 5 etapas navegável (clicável), com passos "Voltar"/"Continuar"/"Criar encontro" no rodapé, e contador "Passo N de 5".

  **Passo 1 — Identidade**
  - Número do encontro: gerado automaticamente (sequencial da paróquia), somente leitura ("auto").
  - Padroeiro do encontro: texto, obrigatório.
  - Diretor espiritual: seleção (pessoa/clero da paróquia), obrigatório.

  **Passo 2 — Local & datas**
  - Local: texto, obrigatório.
  - Data de início / Data de término: data, obrigatórias.
  - Vagas: número, obrigatório.
  - **Regra de negócio exibida na tela:** ao atingir o limite de vagas, novas pré-inscrições entram automaticamente no cadastro de reserva, com prioridade no próximo encontro.

  **Passo 3 — Equipe Dirigente**
  - Composição das 5 pastas (Finanças, Fichas, Palestras, Pós-Encontro, Montagem), cada uma com 2 jovens + 1 casal; indicação visual de pasta completa/incompleta (com vaga em aberto e ação "Adicionar jovem"); diretor espiritual acompanha as 5 pastas.
  - **Validação de tela:** pasta pode ficar incompleta e o wizard permite avançar mesmo assim (aviso na Revisão).

  **Passo 4 — Equipe Comando**
  - Coordenador geral: seleção de pessoa, obrigatório.
  - Casal coordenador: seleção, obrigatório.
  - **Regra de negócio exibida:** após a criação, a Comando enviará convites aos coordenadores de cada equipe; toda recusa exige justificativa e notifica imediatamente a Montagem.

  **Passo 5 — Revisão**
  - Resumo de todos os dados anteriores, com atalho "Editar" para cada bloco.
  - Alerta se alguma pasta da Equipe Dirigente ainda tiver vaga em aberto (não bloqueia a criação).
  - Ação final: "Criar encontro".

  **Tela de sucesso:** confirma criação do próximo número de encontro, informa que a Equipe Dirigente está montada e que o encontro foi entregue à Equipe Comando, que poderá convidar coordenadores; mostra resumo (padroeiro, datas, local, diretor espiritual); ações "Ir para o painel"/"Ver no mapa".
  - **Nota de implementação do protótipo:** o wizard é funcional na navegação, mas sem persistência real de dados entre sessões (dado do HANDOFF do protótipo).

### 2.3 Detalhes do Encontro (`screen_encontro`)
- **Objetivo:** central de informações e navegação de um encontro específico.
- **Descrição funcional (observada nos títulos/abas do protótipo):** exibe abas/seções relacionadas a equipes e outros dados do encontro selecionado (`encTab` controla a aba ativa: observada aba "equipes" por padrão).
- **Ações:** voltar para Lista de Encontros.

## 3. Fluxos

### Fluxo: Criar um novo encontro
- **Objetivo:** abrir um novo ciclo operacional para a paróquia.
- **Ator:** perfil Paróquia.
- **Pré-condições:** encontro anterior da paróquia encerrado ou em fase avançada (regra de permitir múltiplos encontros simultâneos não confirmada — ver pendências); diretor espiritual e Equipe Dirigente disponíveis.
- **Cenário principal:** Paróquia acessa "Novo encontro" → preenche os 5 passos → revisa → confirma criação → sistema gera o próximo número sequencial, cria a Equipe Dirigente e entrega o encontro à Equipe Comando.
- **Fluxos alternativos:** usuário pode voltar e editar qualquer passo antes de confirmar; pode cancelar a qualquer momento.
- **Exceções:** pasta da Equipe Dirigente incompleta — sistema permite prosseguir mesmo assim, com aviso.
- **Pós-condição:** encontro criado, em fase de composição pela Equipe Comando (convites de coordenadores).
- **Regras envolvidas:** RN-ENCONTRO-01 a 04 (abaixo).

## 4. Regras de negócio
- **RN-ENCONTRO-01:** o número do encontro é sequencial e automático por paróquia.
- **RN-ENCONTRO-02:** ao esgotar as vagas configuradas, novas pré-inscrições vão automaticamente para o Cadastro de Reserva.
- **RN-ENCONTRO-03:** a Equipe Dirigente tem exatamente 5 pastas fixas (Finanças, Fichas, Palestras, Pós-Encontro, Montagem), cada uma com 2 jovens + 1 casal, acompanhadas pelo diretor espiritual.
- **RN-ENCONTRO-04:** após a criação, o encontro é entregue à Equipe Comando, que assume a etapa de convite de coordenadores das equipes de serviço.
- **RN-ENCONTRO-05:** o encontro pode ser criado mesmo com pastas da Equipe Dirigente incompletas (não bloqueante).

## 5. Casos de uso
- **UC — Criar encontro:** Ator: Paróquia. Pré-condição: dados de identidade, local, equipe dirigente e comando disponíveis. Fluxo principal: wizard de 5 passos → revisão → confirmação. Pós-condição: encontro criado e entregue à Comando.
- **UC — Consultar encontro:** Ator: qualquer perfil com acesso a Encontros. Fluxo principal: Lista de Encontros → seleciona item → Detalhes do Encontro.

## 6. Regras pendentes de validação
- Regra de permitir (ou não) mais de um encontro ativo simultâneo por paróquia não está definida.
- Regras de aprovação em nível de Diocese/Setorial para criação de encontro (o Painel da Diocese menciona "3 aprovações de encontro aguardando a diocese", sugerindo uma etapa de aprovação superior não representada no wizard) — precisa validação.
- Conteúdo completo e navegação da tela "Detalhes do Encontro" (abas além de "equipes") não foi detalhado no protótipo revisado.
- Regras de edição/cancelamento de um encontro já criado não especificadas.
- Definição de campos obrigatórios vs. opcionais em cada passo do wizard não está formalmente marcada na interface (todos os campos aparentam obrigatórios pelo padrão visual, mas isso precisa confirmação).
