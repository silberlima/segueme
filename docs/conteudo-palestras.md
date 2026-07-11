# Conteúdo do Encontro — Palestras e Palestrantes

## 1. Objetivo
Gerenciar o conteúdo formativo do encontro: os temas oficiais (palestras) e quem os ministra (palestrantes).

## 2. Telas envolvidas

### 2.1 Palestras (`screen_palestras`)
- **Objetivo:** listar os temas oficiais do encontro, em ordem fixa, com palestrante designado.
- **Regra de negócio central:** existem **7 temas oficiais numerados**, em **ordem fixa** (não reordenável pela paróquia) — conteúdo padronizado nacionalmente.
- **Campos por palestra:** número/ordem, tema, palestrante designado.
- **Quem acessa:** perfis com visão de Encontro (Paróquia e Equipe Dirigente/pasta correspondente).

### 2.2 Palestrantes (`screen_palestrantes`)
- **Objetivo:** gerenciar o cadastro de pessoas que ministram as palestras.
- **Componentes:** grid de palestrantes.
- **Tipos de palestrante (regra de negócio):**
  - **Convidado externo** — sem cadastro de seguidor no sistema (pessoa de fora do movimento).
  - **Seguidor** — pessoa já cadastrada no sistema nacional de pessoas.
  - **Clero** — padre/religioso.

## 3. Regras de negócio
- **RN-CONTEUDO-01:** o encontro tem exatamente 7 palestras oficiais, com ordem fixa e definida nacionalmente.
- **RN-CONTEUDO-02:** um palestrante pode ou não ter cadastro de pessoa no sistema — apenas quando é Seguidor; convidados externos não geram cadastro de pessoa.

## 4. Casos de uso
- **UC — Designar palestrante:** Ator: perfil responsável pelo conteúdo do encontro (pasta Palestras da Equipe Dirigente — a confirmar). Fluxo principal: abrir Palestras → selecionar tema → escolher/cadastrar palestrante. Pós-condição: palestra com palestrante definido.

## 5. Regras pendentes de validação
- Os 7 temas oficiais e seu conteúdo/ementa não foram detalhados no protótipo (apenas mencionados como numerados e fixos) — a lista definitiva precisa ser fornecida pelo Product Owner.
- Regra de quem tem permissão para editar a designação de palestrante (Paróquia, pasta Palestras da Equipe Dirigente, ou ambos) não está explicitamente restringida no protótipo.
- Fluxo de cadastro de um Convidado externo (campos mínimos, sem gerar registro de Pessoa) não foi detalhado.
