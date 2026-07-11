# Inscrições

## 1. Objetivo
Gerenciar o funil de inscrição de participantes em um Encontro, desde a pré-inscrição até a confirmação como Seguimista, incluindo o cadastro de reserva quando as vagas se esgotam.

## 2. Telas envolvidas

### 2.1 Pré-inscrições (`screen_preinscricao`)
- **Objetivo:** gerenciar candidatos que solicitaram participar de um encontro, antes do preenchimento da ficha oficial.
- **Quem acessa:** perfil Equipe Fichas (principal responsável) e Montagem/Paróquia como consulta.
- **Componentes:** indicador de ocupação de vagas (barra + "84/90" + "8 em reserva"); abas de status ("Aguardando ficha", "Em análise", "Confirmadas", com contadores); busca por candidato; tabela (Candidato, Idade, Indicado por, Recebida — data relativa, Ação).
- **Ações:** "Nova pré-inscrição"; "Preencher ficha" (por linha, abre Ficha de Inscrição).
- **Regra de negócio:** a ficha só pode ser preenchida pela Equipe Fichas — texto da tela reforça "modelo nacional".

### 2.2 Ficha de Inscrição (`screen_ficha`)
- **Objetivo:** registrar os dados completos de um candidato para confirmar sua inscrição em um encontro.
- **Quem acessa:** Equipe Fichas.
- **Estrutura:** 4 seções + painel lateral com status do candidato e stepper do "Fluxo da inscrição" (Pré-inscrição concluída → Preenchimento da ficha em andamento → Confirmação → Participação).

  **Seção 1 — Dados pessoais:** nome completo (texto, obrigatório), data de nascimento (data, obrigatório), telefone (texto/máscara de telefone, obrigatório), estado civil (seleção, obrigatório), paróquia (seleção, obrigatório).

  **Seção 2 — Saúde & restrições:** alergias (texto, opcional), restrição alimentar (texto, opcional; placeholder "Nenhuma"), medicação de uso contínuo (texto, opcional; placeholder "Nenhuma").

  **Seção 3 — Contato de emergência:** nome (texto, obrigatório), telefone (texto, obrigatório), parentesco (texto, obrigatório).

  **Seção 4 — Competências:** chips selecionáveis do catálogo nacional (múltipla escolha, opcional) — usados pela Equipe Montagem para compor equipes.

- **Ações:** "Confirmar inscrição" (salva e avança o status), "Cancelar" (volta às pré-inscrições sem salvar).
- **Mensagem de conformidade exibida:** "Os dados desta ficha integram o cadastro nacional e nunca são apagados."
- **Regra de negócio:** ao confirmar, a situação da pessoa muda de "Pré-inscrito" para "Seguimista".

### 2.3 Cadastro de Reserva (`screen_reserva`)
- **Objetivo:** gerenciar a fila de prioridade de candidatos que não couberam no limite de vagas do encontro.
- **Componentes:** aviso de vagas esgotadas ("90/90") com explicação da prioridade automática; tabela (Ordem, Pessoa, Paróquia, Na fila desde, Ação).
- **Ações:** "Promover à vaga" (por linha — move a pessoa da reserva para inscrição confirmada quando uma vaga se abre).
- **Regra de negócio:** a fila obedece ordem de chegada (FIFO); a prioridade vale automaticamente para o próximo encontro da mesma paróquia.

### 2.4 Participantes (`screen_participantes`)
- **Objetivo:** consolidar visualmente o funil completo de inscrição de um encontro e listar os participantes finais com status de pagamento.
- **Componentes:** funil (Pré-inscrição → Fichas → Confirmados → Reserva → Seguidores) com contadores por etapa; tabela final de participantes com coluna de pagamento.
- **Quem acessa:** perfil Equipe Fichas.

## 3. Fluxos

### Fluxo: Inscrever um novo participante
- **Objetivo:** levar uma pessoa da pré-inscrição à confirmação como seguimista.
- **Pré-condição:** pessoa cadastrada (ou cadastrada no ato da pré-inscrição — fluxo exato de entrada não detalhado); encontro com vagas ou em cadastro de reserva.
- **Cenário principal:** candidato é pré-inscrito → aparece em "Aguardando ficha" → Equipe Fichas preenche a Ficha de Inscrição → confirma → pessoa vira "Seguimista" → passa a integrar o funil de Participantes.
- **Fluxo alternativo (vagas esgotadas):** pré-inscrição excedente entra automaticamente no Cadastro de Reserva → aguarda uma vaga surgir (desistência etc.) → é promovida manualmente pela Equipe Fichas.
- **Pós-condição:** pessoa com situação atualizada; disponível para compor equipes (após participar como Seguidor pela primeira vez).

## 4. Regras de negócio
- **RN-INSCRICAO-01:** a ficha de inscrição só pode ser preenchida pela Equipe Fichas.
- **RN-INSCRICAO-02:** ao confirmar a ficha, a pessoa passa de "Pré-inscrito" para "Seguimista".
- **RN-INSCRICAO-03:** ao esgotar o limite de vagas do encontro, novas pré-inscrições entram automaticamente no Cadastro de Reserva.
- **RN-INSCRICAO-04:** a ordem do Cadastro de Reserva segue a ordem de chegada; a prioridade é automática para o próximo encontro.
- **RN-INSCRICAO-05:** os dados da ficha integram o cadastro nacional único e nunca são apagados.

## 5. Casos de uso
- **UC — Preencher ficha de inscrição:** Ator: Equipe Fichas. Pré-condição: candidato em "Aguardando ficha". Fluxo principal: abrir Pré-inscrições → "Preencher ficha" → completar as 4 seções → "Confirmar inscrição". Pós-condição: candidato confirmado como Seguimista.
- **UC — Promover candidato da reserva:** Ator: Equipe Fichas. Pré-condição: vaga disponível no encontro. Fluxo principal: Cadastro de Reserva → "Promover à vaga" no candidato de maior prioridade. Pós-condição: candidato passa a inscrição confirmada.

## 6. Regras pendentes de validação
- Critério exato de quando uma vaga "se abre" para promover alguém da reserva (desistência, expansão de vagas) não especificado.
- Regras de validação de campos da Ficha (máscara de telefone, formato de data de nascimento, idade mínima) não especificadas.
- Regra de pagamento (valores, formas, prazos) citada apenas como coluna na tabela de Participantes, sem detalhamento funcional.
- Mensagens de erro/sucesso do fluxo de confirmação de ficha não especificadas.
- Fluxo de "Nova pré-inscrição" (quem pode registrar, campos mínimos exigidos nesse primeiro contato) não detalhado — o protótipo mostra apenas o botão, sem o formulário correspondente.
