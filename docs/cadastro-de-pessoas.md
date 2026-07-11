# Cadastro de Pessoas

## 1. Objetivo
Manter o cadastro nacional único de pessoas do movimento, com seu histórico completo, competências, vínculos de casal e situação atual.

## 2. Telas envolvidas

### 2.1 Lista de Pessoas (`screen_pessoas`)
- **Objetivo:** localizar, listar e iniciar o cadastro de pessoas.
- **Quem acessa:** todos os perfis (item "Pessoas"/"Seguidores" aparece no menu de quase todos os perfis).
- **Componentes:** busca por nome/paróquia/competência, botão de filtros, tabela paginada (Pessoa com avatar/idade/estado civil, Paróquia atual, Situação — badge Seguimista/Seguidor/Inscrito, Competências — chips, Encontros — contagem), paginação.
- **Estados de tela (modelados explicitamente via abas de demonstração):** Dados (carregado com sucesso), Carregando (skeleton shimmer por linha), Vazio (nenhum resultado — com CTA "Limpar filtros"/"Nova pessoa"), Erro (falha de conexão — CTA "Tentar novamente").
- **Ações:** "Nova pessoa" (vai para Cadastro de Pessoa), clique na linha (vai para Perfil Completo).
- **Regra de negócio:** o histórico da pessoa nunca é apagado, mesmo com transferência de paróquia (indicado com tag "· transferida"/"· emprestado" na paróquia atual).

### 2.2 Cadastro de Pessoa (`screen_cadastro`)
- **Objetivo:** registrar uma nova pessoa no cadastro nacional.
- **Quem acessa:** perfis com permissão de gestão de pessoas (observado a partir da Lista de Pessoas, acessível a partir de qualquer perfil que tenha o item "Pessoas" no menu — permissão granular por perfil não detalhada no protótipo).
- **Descrição funcional:** formulário em 4 seções + painel lateral, seguindo o mesmo padrão de campos da Ficha de Inscrição (dados pessoais, saúde/restrições, contato de emergência, competências).
- **Campos obrigatórios (inferidos do padrão da Ficha):** nome completo, data de nascimento, telefone, paróquia.
- **Campos opcionais:** alergias, restrição alimentar, medicação, competências.
- **Valor padrão / regra de negócio:** uma pessoa recém-cadastrada nasce com situação **"Pré-inscrito"**.
- **Ações:** salvar cadastro, cancelar.

### 2.3 Perfil Completo (`screen_perfil`)
- **Objetivo:** exibir todos os dados e o histórico consolidado de uma pessoa.
- **Quem acessa:** qualquer perfil que acesse a Lista de Pessoas; é também o painel inicial do perfil **Seguidor** (visão do próprio perfil).
- **Conteúdo:** dados cadastrais, situação atual, competências, navegação para Histórico.
- **Ações:** voltar para Lista de Pessoas.

### 2.4 Histórico (`screen_historico`)
- **Objetivo:** listar cronologicamente todos os eventos relevantes da trajetória de uma pessoa no movimento.
- **Quem acessa:** a partir do Perfil Completo.
- **Tipos de evento observados:** inscrição no cadastro nacional, primeira participação como seguidor, primeira equipe como equipista, tornar-se seguimista, participação em equipes (por encontro), vínculo de casal registrado, empréstimo a outra paróquia (com autorização do Setorial e retorno registrado), papel de coordenador.
- **Regra de negócio:** o histórico é append-only — nenhum evento é removido, inclusive transferências e empréstimos.

### 2.5 Casais (`screen_casais`)
- **Objetivo:** gerenciar vínculos de casal entre duas pessoas cadastradas.
- **Descrição funcional:** lista de casais com dados agregados (situação, encontros, paróquia, competências).
- **Regra de negócio:** casal é sempre representado por 2 cadastros de pessoa distintos e vinculados; a listagem também aceita uma pessoa individual com estado civil "casado" sem casal vinculado no sistema (ex.: cônjuge fora do movimento).

### 2.6 Catálogo Nacional de Competências (`screen_competencias`)
- **Objetivo:** apresentar o catálogo nacional de dons/habilidades e as pessoas que os possuem.
- **Descrição funcional:** 8 competências nacionais catalogadas (observadas: Canta, Cozinha, Violão, Fotografia, Decoração, Som/Teclado, Motorista, Informática, Liturgia — conjunto exato a confirmar), cada uma com contagem de pessoas e lista de detalhe.
- **Quem acessa:** perfil Montagem tem o item no menu; usado como apoio à composição de equipes e ao ranqueamento de Sugestões Inteligentes.
- **Ações:** ver pessoas por competência.

## 3. Fluxos

### Fluxo: Cadastrar nova pessoa
- **Objetivo:** registrar alguém pela primeira vez no sistema.
- **Pré-condição:** usuário tem permissão de cadastro.
- **Cenário principal:** usuário clica "Nova pessoa" → preenche formulário em 4 seções → salva → pessoa é criada com situação "Pré-inscrito" → retorna à Lista de Pessoas.
- **Pós-condição:** pessoa passa a existir no cadastro nacional único, disponível para qualquer paróquia que a busque.

### Fluxo: Consultar histórico de uma pessoa
- **Cenário principal:** usuário busca/seleciona pessoa na Lista → abre Perfil Completo → abre Histórico → visualiza linha do tempo de eventos.

## 4. Regras de negócio
- **RN-PESSOA-01:** o cadastro de pessoa é único e nacional; nunca duplicado por paróquia.
- **RN-PESSOA-02:** o histórico de uma pessoa nunca é apagado ou sobrescrito.
- **RN-PESSOA-03:** casal = 2 cadastros vinculados.
- **RN-PESSOA-04:** uma nova pessoa nasce com situação "Pré-inscrito".
- **RN-PESSOA-05:** a situação de uma pessoa evolui conforme o funil de inscrição (ver `inscricoes.md`): Pré-inscrito → (ficha) → Seguimista → (1ª equipe) → Seguidor.

## 5. Casos de uso
- **UC — Cadastrar pessoa:** Ator: equipe com permissão de cadastro. Pré-condição: dados básicos da pessoa disponíveis. Fluxo principal: acessar Lista de Pessoas → "Nova pessoa" → preencher e salvar. Pós-condição: pessoa criada como "Pré-inscrito".
- **UC — Consultar pessoa:** Ator: qualquer perfil com acesso a Pessoas. Fluxo principal: buscar/filtrar na lista → abrir Perfil Completo → opcionalmente abrir Histórico.

## 6. Regras pendentes de validação
- Regras de validação de campos (formato de telefone, data, obrigatoriedade exata) não especificadas — apenas inferidas do padrão visual da Ficha de Inscrição.
- Quais perfis têm permissão de **criar/editar** pessoas (vs. apenas visualizar) não está explicitamente restringido no protótipo.
- Regra de deduplicação de cadastro (evitar pessoa duplicada) não especificada.
- Lista exata e definitiva das 8 competências do catálogo nacional precisa ser confirmada com o Product Owner.
- Comportamento de edição de pessoa existente (tela de edição vs. reaproveitar tela de cadastro) não está modelado — apenas criação nova.
