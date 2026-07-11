# Transferências, Empréstimos e Solicitações ao Setorial

## 1. Objetivo
Gerenciar a movimentação de pessoas entre paróquias — de forma definitiva (Transferência) ou temporária para um encontro específico (Empréstimo) — sempre com autorização do Setorial.

## 2. Telas envolvidas

### 2.1 Transferências (`screen_transferencias`)
- **Objetivo:** solicitar e acompanhar a mudança definitiva de vínculo de uma pessoa entre paróquias.
- **Quem acessa:** perfis de Pessoas/Paróquia (solicitação) e Setorial (decisão).
- **Regra de negócio:** toda transferência exige autorização do Setorial; o histórico completo da pessoa é preservado após a mudança de paróquia.
- **Conteúdo observado no Painel do Setor:** card de solicitação com nome da pessoa, paróquia de origem e destino, contexto ("todo o histórico será preservado"), ações Aprovar/Recusar.

### 2.2 Empréstimos (`screen_emprestimos`)
- **Objetivo:** permitir que uma equipe empreste temporariamente um seguidor de outra paróquia para compor uma equipe do encontro corrente, sem transferência definitiva.
- **Fluxo em duas etapas, por perfil (regra de negócio central):**
  1. **Perfil Montagem:** cria uma "Nova solicitação" de empréstimo dirigida ao Setorial (informando a equipe/vaga e a habilidade necessária) → aguarda → recebe as sugestões de candidatos devolvidas pelo Setorial → clica "Adicionar às equipes" para efetivar.
  2. **Perfil Setorial:** vê as solicitações de empréstimo de todas as paróquias do setor → seleciona seguidores de outras paróquias como candidatos (linhas clicáveis, seleção múltipla) → aplica filtros por Paróquia e por Habilidade (chips, derivados dinamicamente dos candidatos disponíveis; exibe estado vazio quando nenhum candidato casa com o filtro) → clica "Enviar à Montagem".
- **Item de menu:** "Empréstimos" aparece tanto no menu do perfil Montagem quanto no do Setorial, cada um com sua própria visão da tela.
- **Regra de negócio:** todo empréstimo exige autorização/participação ativa do Setorial; o histórico de origem da pessoa é preservado (fica registrado como "emprestado" na paróquia de destino, ver Lista de Pessoas).

### 2.3 Solicitações (`screen_solicitacoes`, perfil Setorial)
- **Objetivo:** inbox único de itens pendentes de decisão do Setorial (Transferências e Empréstimos consolidados).
- **Componentes:** card por solicitação (tipo — Transferência/Empréstimo, tempo relativo, descrição textual da solicitação), botões "Autorizar"/"Aprovar" e "Recusar".
- **Badge de menu:** contagem de solicitações pendentes (ex.: "Solicitações · 4"), refletida também no dashboard Setorial.

## 3. Fluxos

### Fluxo: Solicitar transferência definitiva
- **Objetivo:** mudar o vínculo de uma pessoa para outra paróquia.
- **Pré-condição:** pessoa cadastrada, vinculada a uma paróquia de origem.
- **Cenário principal:** solicitação de transferência é registrada (origem exata do gatilho — se pela própria pessoa, pela paróquia de origem ou destino — não detalhada) → aparece no inbox de Solicitações do Setorial → Setorial aprova ou recusa.
- **Pós-condição (aprovado):** pessoa passa a pertencer à paróquia de destino; histórico preservado e visível.
- **Pós-condição (recusado):** pessoa permanece na paróquia de origem.

### Fluxo: Empréstimo de seguidor entre paróquias
- **Objetivo:** suprir uma vaga de equipe com alguém de fora da paróquia, sem mudança definitiva.
- **Ator principal:** Equipe Montagem (solicitante) e Setorial (decisor/mediador).
- **Pré-condição:** vaga em aberto numa equipe cujo candidato ideal está em outra paróquia do mesmo setor.
- **Cenário principal:**
  1. Montagem identifica necessidade (via Sugestões Inteligentes, tag "Requer empréstimo do setor") e registra Nova Solicitação de empréstimo ao Setorial, informando habilidade/equipe.
  2. Setorial visualiza as solicitações de todas as paróquias do setor, filtra candidatos por Paróquia e Habilidade, seleciona os seguidores elegíveis.
  3. Setorial envia a seleção de volta à Montagem ("Enviar à Montagem").
  4. Montagem recebe as sugestões devolvidas e clica "Adicionar às equipes" para efetivar o empréstimo.
- **Fluxo alternativo (sem candidatos):** filtro do Setorial não encontra ninguém que combine — tela exibe estado vazio.
- **Pós-condição:** pessoa passa a constar como "emprestada" na equipe/paróquia de destino para aquele encontro; seu vínculo de origem não muda.

## 4. Regras de negócio
- **RN-MOVIMENTO-01:** toda Transferência exige autorização do Setorial.
- **RN-MOVIMENTO-02:** todo Empréstimo exige participação ativa do Setorial (seleção e envio de candidatos).
- **RN-MOVIMENTO-03:** o histórico de uma pessoa é sempre preservado, independentemente de transferência ou empréstimo.
- **RN-MOVIMENTO-04:** o fluxo de Empréstimo é obrigatoriamente bidirecional — a Montagem não pode adicionar diretamente um seguidor de outra paróquia sem passar pelo Setorial.
- **RN-MOVIMENTO-05:** os filtros de Paróquia e Habilidade no fluxo de Empréstimo (visão Setorial) são derivados dinamicamente dos candidatos existentes, não de uma lista fixa.

## 5. Casos de uso
- **UC — Aprovar/recusar transferência:** Ator: Setorial. Fluxo principal: abrir Solicitações → avaliar card de transferência → Aprovar ou Recusar. Pós-condição: pessoa transferida (ou não), histórico preservado.
- **UC — Solicitar empréstimo:** Ator: Montagem. Fluxo principal: abrir Empréstimos → Nova solicitação → informar equipe/habilidade. Pós-condição: solicitação enviada ao Setorial.
- **UC — Selecionar e enviar candidatos de empréstimo:** Ator: Setorial. Fluxo principal: abrir Empréstimos → ver solicitações do setor → filtrar por paróquia/habilidade → selecionar candidatos → Enviar à Montagem. Pós-condição: Montagem recebe sugestões.
- **UC — Efetivar empréstimo:** Ator: Montagem. Pré-condição: sugestões recebidas do Setorial. Fluxo principal: revisar sugestões → Adicionar às equipes. Pós-condição: pessoa emprestada compõe a equipe.

## 6. Regras pendentes de validação
- Quem inicia (gatilho) uma solicitação de Transferência não está claramente modelado no protótipo (pessoa, paróquia de origem, ou paróquia de destino?) — precisa validação.
- Prazo de vigência de um Empréstimo (ele termina automaticamente ao fim do encontro?) não especificado.
- Regra de limite de quantos empréstimos uma paróquia pode conceder simultaneamente não especificada.
- Notificação formal às pessoas envolvidas (pessoa emprestada, paróquia de origem) sobre a decisão do Setorial não está detalhada — apenas a notificação sistêmica interna (drawer de notificações) é observada.
