# Convites, Aceites, Recusas e Coordenação de Equipe

## 1. Objetivo
Gerenciar o ciclo de convite de pessoas para papéis de coordenador e integrante nas equipes de um encontro, incluindo acompanhamento de aceites e tratamento de recusas.

## 2. Telas envolvidas

### 2.1 Convites (`screen_convites`)
- **Objetivo:** emitir e acompanhar convites enviados às pessoas para compor as equipes do encontro.
- **Quem acessa:** perfil Equipe Comando (convites de coordenador) e Coordenador de equipe (convites de integrante, ver 2.5).
- **Dados por convite (modelo observado):** pessoa, equipe, papel (coordenador/integrante), status (pendente/aceito/recusado), data de envio, data de resposta, justificativa (quando recusado).
- **Ações:** "Novo convite" (a partir do painel de Comando).
- **Regra de negócio:** o fluxo formal é Montagem → monta a equipe → Comando convida o coordenador → coordenador aceita → coordenador convida os integrantes.

### 2.2 Aceites (`screen_aceites`)
- **Objetivo:** listar convites já aceitos, com contexto de equipe e papel.
- **Quem acessa:** Equipe Comando.

### 2.3 Recusas (`screen_recusas`)
- **Objetivo:** listar convites recusados, com a justificativa obrigatória de cada recusa.
- **Quem acessa:** Equipe Comando.
- **Regra de negócio:** toda recusa exige justificativa e dispara notificação imediata à Equipe Montagem, que trata a vaga como uma substituição pendente (ver `montagem-de-equipes.md`).

### 2.4 Equipe — detalhe individual (`screen_equipe`)
- **Objetivo:** ver o detalhe de uma única equipe (variação com foco em cabeçalho de mínimo/máximo/pré-requisito, roster e vaga em aberto levando a sugestões).
- **Conteúdo:** mesmo modelo de dados do workspace de Montagem (`screen_equipes`), em visão de detalhe/individual, acessível a partir de outros contextos (ex.: perfil do Coordenador).

### 2.5 Coordenador (`screen_coordenador`)
- **Objetivo:** painel de trabalho do Coordenador de uma equipe específica.
- **Quem acessa:** perfil da pessoa designada como coordenador daquela equipe (papel dentro de uma equipe, não um perfil de acesso geral do sistema).
- **Conteúdo:** estatísticas da equipe, ação de "convidar integrante", lista de convites da equipe com opção de "reenviar".
- **Ações:** convidar novo integrante; reenviar convite pendente.
- **Regra de negócio:** somente após aceitar o convite de coordenador (emitido pela Comando) a pessoa pode convidar os integrantes de sua equipe.

## 3. Fluxos

### Fluxo: Convite de coordenador
- **Objetivo:** preencher a liderança de uma equipe.
- **Ator:** Equipe Comando.
- **Pré-condição:** equipe definida pela Montagem, aguardando coordenador.
- **Cenário principal:** Comando emite convite ao candidato a coordenador → candidato aceita → pessoa passa a ter acesso ao Painel do Coordenador daquela equipe.
- **Fluxo alternativo (recusa):** candidato recusa com justificativa → Comando registra a recusa → Montagem é notificada → Montagem trata como necessidade de substituição.
- **Pós-condição:** equipe com coordenador definido (ou pendência registrada).

### Fluxo: Convite de integrante
- **Objetivo:** preencher vagas de integrante de uma equipe.
- **Ator:** Coordenador da equipe (após aceitar seu próprio convite).
- **Cenário principal:** coordenador convida integrante (indicado pela Montagem/sugestão) → integrante aceita ou recusa.
- **Fluxo alternativo (recusa):** mesma regra de justificativa obrigatória + notificação à Montagem.
- **Pós-condição:** integrante com status atualizado no roster da equipe.

## 4. Regras de negócio
- **RN-CONVITE-01:** ordem obrigatória do fluxo de convite: Montagem monta a equipe → Comando convida coordenador → coordenador aceita → coordenador convida integrantes.
- **RN-CONVITE-02:** toda recusa de convite exige justificativa textual.
- **RN-CONVITE-03:** toda recusa notifica imediatamente a Equipe Montagem.
- **RN-CONVITE-04:** o Coordenador só pode convidar integrantes de sua própria equipe.

## 5. Casos de uso
- **UC — Convidar coordenador:** Ator: Comando. Pré-condição: equipe sem coordenador. Fluxo principal: emitir convite → aguardar resposta. Pós-condição: coordenador definido ou recusa registrada.
- **UC — Convidar integrante:** Ator: Coordenador de equipe. Pré-condição: convite de coordenador aceito; vaga de integrante em aberto. Fluxo principal: convidar pessoa indicada → aguardar resposta. Pós-condição: integrante definido ou recusa registrada.
- **UC — Tratar recusa:** Ator: Montagem (reativo à notificação). Fluxo principal: recebe notificação de recusa → abre Substituições → registra novo candidato. Pós-condição: vaga preenchida por outra pessoa.

## 6. Regras pendentes de validação
- Prazo/expiração de um convite pendente sem resposta não está especificado.
- Regra de reenvio de convite (quantas vezes, se reinicia prazo) não especificada — apenas a ação "reenviar" existe na tela do Coordenador.
- Permissão exata de quem pode acessar o Painel do Coordenador (é um perfil de acesso ou uma visão condicionada ao papel dentro da equipe?) precisa validação — no protótipo, o "perfil" de acesso trocável não inclui "Coordenador" como opção da lista de 10 perfis, sugerindo que é um estado/papel dentro de outro perfil (a confirmar).
