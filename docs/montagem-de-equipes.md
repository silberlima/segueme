# Montagem de Equipes

## 1. Objetivo
Permitir que a Equipe Montagem componha as equipes de serviço de um encontro (coordenador + integrantes), apoiada por sugestões automáticas de candidatos, e registre substituições quando necessário.

## 2. Telas envolvidas

### 2.1 Equipes — Workspace de Montagem (`screen_equipes`)
- **Objetivo:** visualizar e (para a Montagem) editar a composição de todas as equipes de um encontro.
- **Quem acessa:** todos os perfis com item "Equipes" no menu (visualização); apenas o perfil **Montagem** vê as ações de edição (adicionar integrante, trocar coordenador, transferir/substituir) e o botão "Sugestões inteligentes".
- **Layout:** lista lateral de equipes (nome, ícone, contagem preenchida/máxima, status Completa/Incompleta) + painel de detalhe da equipe selecionada.
- **Detalhe da equipe:** nome, ícone, "X de Y integrantes · mínimo Z", pré-requisito de elegibilidade em destaque; card do Coordenador (nome, tempo de participação, status, botão de trocar — só Montagem); grid de Integrantes (nome, tempo de participação, status aceito/pendente/recusado, menu de ações — só Montagem); cards de "Vaga em aberto" tracejados que levam às Sugestões Inteligentes.
- **Ações (perfil Montagem):** abrir sugestões para vaga em aberto; trocar coordenador; transferir/substituir integrante.
- **Regra de negócio:** as equipes (nome, mínimo, máximo, pré-requisito) são definidas nacionalmente; a paróquia/Montagem apenas preenche vagas, não cria ou renomeia equipes.

### 2.2 Sugestões Inteligentes — tela cheia (`screen_sugestoes`)
- **Objetivo:** apresentar, para cada vaga em aberto do encontro, uma lista ranqueada de candidatos elegíveis.
- **Quem acessa:** exclusivamente o perfil **Montagem** — a tela exibe um selo "Visível só para a Montagem".
- **Layout:** lista lateral de "Vagas em aberto" (equipe + quantidade de vagas) + painel de candidatos ranqueados para a vaga selecionada.
- **Filtros:** chips de filtro sobre os candidatos (ex.: por competência, disponibilidade — 4 filtros observados, valores exatos variam por vaga).
- **Cálculo do match (regra de negócio central):** score de 0 a 100, combinando 4 fatores com pesos fixos:
  - Histórico em equipes semelhantes — 35%
  - Competências exigidas — 30%
  - Frequência no pós-encontro — 20%
  - Cumprimento do pré-requisito da equipe — 15%
- **Cada candidato exibe:** posição no ranking, nome, meta (situação/idade/paróquia), score e barra de match, tags de justificativa (ex.: "Cumpre pré-requisito", "N encontros na equipe", "Requer empréstimo do setor" quando o candidato é de outra paróquia).
- **Ações:** "Adicionar à equipe" (efetiva a indicação), "Perfil" (abre o Perfil Completo do candidato).
- **Aviso fixo na tela:** "As sugestões nunca substituem a decisão da Montagem" — reforça que a indicação automática é apoio, não decisão automática.
- **Regra de negócio:** quando o candidato sugerido pertence a outra paróquia, a tag "Requer empréstimo do setor" sinaliza que a adição depende de autorização do Setorial (ver `transferencias-e-emprestimos.md`).

### 2.3 Substituições & Transferências (`screen_substituicoes`)
- **Objetivo:** registrar a troca de um integrante por outro dentro de uma equipe, tipicamente motivada por recusa de convite.
- **Quem acessa:** perfil Montagem.
- **Componentes — "Nova substituição":** card "Sai" (pessoa que está saindo, com motivo, ex.: "recusou"), seta central, card "Entra" (abre Sugestões Inteligentes para escolher substituto), campo de Justificativa (texto, **obrigatório**), botão "Registrar substituição".
- **Componentes — "Histórico recente":** lista de substituições/transferências concluídas (quem substituiu quem, equipe, motivo, tempo relativo, status "Concluída").
- **Regra de negócio:** toda substituição por recusa exige justificativa; a Montagem é notificada imediatamente e o histórico de ambas as pessoas envolvidas é preservado.

### 2.4 Catálogo de Competências
Ver `cadastro-de-pessoas.md` (seção 2.6) — usado como insumo do ranqueamento de Sugestões Inteligentes.

## 3. Fluxos

### Fluxo: Preencher vaga em aberto com sugestão inteligente
- **Objetivo:** completar a composição de uma equipe.
- **Ator:** Equipe Montagem.
- **Pré-condição:** equipe com vaga em aberto (abaixo do máximo).
- **Cenário principal:** Montagem abre a equipe → clica em "Vaga em aberto" → sistema mostra candidatos ranqueados por match → Montagem avalia score e tags → clica "Adicionar à equipe" no candidato escolhido.
- **Fluxo alternativo:** Montagem pode ignorar a sugestão e escolher outro candidato via busca de pessoas (fluxo de escolha manual não detalhado no protótipo).
- **Exceção:** candidato de outra paróquia requer autorização do Setorial antes de ser efetivamente adicionado (fluxo de Empréstimo).
- **Pós-condição:** integrante adicionado à equipe; contagem preenchida/máxima atualizada.

### Fluxo: Registrar substituição
- **Objetivo:** trocar um integrante que recusou ou ficou indisponível.
- **Ator:** Equipe Montagem.
- **Pré-condição:** convite recusado ou integrante indisponível.
- **Cenário principal:** Montagem abre "Nova substituição" → seleciona quem sai → escolhe substituto via Sugestões Inteligentes → preenche justificativa → confirma.
- **Pós-condição:** substituição registrada no histórico; ambas as pessoas mantêm seu histórico individual preservado.

## 4. Regras de negócio
- **RN-MONTAGEM-01:** somente a Equipe Montagem altera a composição das equipes de um encontro.
- **RN-MONTAGEM-02:** sugestões inteligentes são visíveis exclusivamente para o perfil Montagem.
- **RN-MONTAGEM-03:** o score de match é composto por 4 fatores com pesos fixos (35/30/20/15%) — ver seção 2.2.
- **RN-MONTAGEM-04:** as sugestões automáticas são apoio à decisão; a escolha final é sempre humana (Montagem).
- **RN-MONTAGEM-05:** toda substituição decorrente de recusa exige justificativa textual obrigatória.
- **RN-MONTAGEM-06:** substituição/transferência de integrante notifica a Montagem e preserva o histórico de ambas as pessoas.
- **RN-MONTAGEM-07:** equipes têm mínimo e máximo de integrantes e um pré-requisito de elegibilidade definidos nacionalmente (não editáveis pela Montagem).

## 5. Casos de uso
- **UC — Compor equipe via sugestão inteligente:** Ator: Montagem. Fluxo principal: abrir equipe com vaga → abrir sugestões → adicionar candidato. Pós-condição: vaga preenchida.
- **UC — Substituir integrante:** Ator: Montagem. Fluxo principal: nova substituição → sai/entra → justificativa → registrar. Pós-condição: substituição registrada, histórico preservado.

## 6. Regras pendentes de validação
- Fórmula exata de cada um dos 4 fatores do score (ex.: como "frequência no pós-encontro" é calculada numericamente) não está especificada, apenas o peso relativo.
- Regra de bloqueio (ou não) de "Confirmar montagem" enquanto houver equipes incompletas não está clara — o dashboard permite a ação mesmo com pendências.
- Regra de o que acontece quando uma equipe atinge o máximo de integrantes (a vaga simplesmente desaparece da lista?) não detalhada.
- Fluxo de escolha manual de candidato (fora da lista de sugestões) não está modelado no protótipo.
