# Entidades do Negócio

Modelagem conceitual apenas — não representa modelagem de banco de dados.

## Pessoa
**Descrição:** Cadastro nacional único de um indivíduo vinculado ao Movimento Segue-me. **Responsabilidade:** manter identidade, dados de contato, dados de saúde, competências e situação (Inscrito, Pré-inscrito, Seguimista, Seguidor) ao longo de toda a vida no movimento; seu histórico nunca é apagado, mesmo com transferência de paróquia.
**Atributos conceituais:** nome completo, CPF (chave natural do cadastro: obrigatório, único e validado), data de nascimento, telefone, Instagram (opcional), estado civil, paróquia atual, endereço residencial (CEP, logradouro, número, complemento opcional, bairro, cidade, UF), situação/status, alergias, restrição alimentar, medicação de uso contínuo, contato de emergência (nome/telefone/parentesco), competências/dons, contagem de encontros participados, indicado por (pessoa que sugeriu a pré-inscrição).
**Relacionamentos:** pertence a uma Paróquia; pode compor um Casal; participa de Encontros; ocupa posições em Equipes; gera registros de Histórico; pode estar em Empréstimo ou Transferência.

## Casal
**Descrição:** Vínculo entre duas Pessoas casadas, ambas com cadastro próprio.
**Responsabilidade:** representar a unidade familiar para fins de composição de equipes (ex.: casal na Equipe Dirigente).
**Atributos conceituais:** as duas pessoas vinculadas, data do vínculo.
**Relacionamentos:** duas Pessoas.

## Paróquia
**Descrição:** Unidade local do movimento, vinculada a um Setor e uma Diocese/Arquidiocese.
**Responsabilidade:** possuir seus próprios Encontros, Equipes (por encontro) e Seguidores.
**Atributos conceituais:** nome, setor, diocese, quantidade de seguidores, encontros realizados.
**Relacionamentos:** pertence a um Setor; tem muitas Pessoas; tem muitos Encontros.

## Setor (Setorial)
**Descrição:** Agrupamento de Paróquias dentro de uma Diocese, com papel de autorização (transferências e empréstimos).
**Atributos conceituais:** nome (ex.: Setor Leste), diocese, paróquias vinculadas.
**Relacionamentos:** pertence a uma Diocese; agrupa Paróquias; aprova Solicitações.

## Diocese / Arquidiocese
**Descrição:** Unidade eclesiástica regional que agrupa Setores.
**Atributos conceituais:** nome, quantidade de setores, quantidade de paróquias, quantidade de seguidores.
**Relacionamentos:** agrupa Setores; pertence à visão Nacional.

## Encontro
**Descrição:** Evento central de operação do movimento, organizado por uma paróquia, com número sequencial próprio da paróquia (ex.: "62º Encontro").
**Responsabilidade:** ser o objeto que agrega Equipe Dirigente, Equipe Comando, Equipes de serviço, Inscrições e Financeiro.
**Atributos conceituais:** número, padroeiro, datas (início/fim), local, diretor espiritual, limite de vagas, status/fase (em preparação, montagem de equipes, inscrições abertas, ativo, encerrado), coordenador geral (casal + pessoa).
**Relacionamentos:** pertence a uma Paróquia; tem uma Equipe Dirigente (5 pastas); tem uma Equipe Comando; tem muitas Equipes de serviço; tem Inscrições; tem lançamentos Financeiros; tem Campanhas de arrecadação.

## Equipe (de serviço no Encontro)
**Descrição:** Grupo de trabalho definido nacionalmente (nome, mínimo/máximo de integrantes, pré-requisito de elegibilidade) que serve durante um Encontro específico. Exemplos observados: Sala, Som, Liturgia, Cozinha, Acolhida, Secretaria.
**Responsabilidade:** ser composta (coordenador + integrantes) exclusivamente pela Equipe Montagem; possuir vagas em aberto até atingir o mínimo/máximo.
**Atributos conceituais:** nome, ícone/tipo, mínimo, máximo, pré-requisito, coordenador, lista de integrantes com status (aceito/pendente/recusado), vagas em aberto.
**Relacionamentos:** pertence a um Encontro; tem um Coordenador (Pessoa); tem Integrantes (Pessoas); recebe Convites; é alvo de Substituições.

## Equipe Dirigente (do Encontro)
**Descrição:** Estrutura fixa de 5 "pastas" (Finanças, Fichas, Palestras, Pós-Encontro, Montagem) formada antes da criação do encontro, cada uma com 2 jovens + 1 casal, acompanhados pelo diretor espiritual.
**Relacionamentos:** parte da criação do Encontro (etapa do wizard); entrega o encontro pronto à Equipe Comando.

## Equipe Comando (do Encontro)
**Descrição:** Equipe responsável pela execução do encontro: convida coordenadores das equipes de serviço e acompanha aceites/recusas.
**Atributos conceituais:** coordenador geral, casal coordenador, roster com status.
**Relacionamentos:** recebe o Encontro pronto da Equipe Dirigente; envia Convites; recebe notificações de Recusa (repassadas à Montagem).

## Inscrição / Pré-inscrição
**Descrição:** Solicitação de participação de uma Pessoa em um Encontro como seguidor, antes da confirmação.
**Responsabilidade:** avançar por um funil (Pré-inscrição → Ficha em análise → Confirmado/Seguimista) até esgotar vagas, quando passa a Cadastro de Reserva.
**Atributos conceionais:** candidato (Pessoa), indicado por, data de recebimento, status.
**Relacionamentos:** pertence a um Encontro; referencia uma Pessoa; gera uma Ficha de Inscrição.

## Ficha de Inscrição
**Descrição:** Formulário nacional único preenchido exclusivamente pela Equipe Fichas para confirmar uma pré-inscrição.
**Atributos conceituais:** dados pessoais, saúde/restrições, contato de emergência, competências marcadas.
**Relacionamentos:** referencia uma Pessoa e um Encontro; ao ser confirmada, muda a situação da Pessoa para "Seguimista".

## Cadastro de Reserva
**Descrição:** Fila de prioridade para pessoas cuja pré-inscrição não coube no limite de vagas de um encontro.
**Atributos conceituais:** posição na fila, pessoa, data de entrada na fila.
**Relacionamentos:** referencia Pessoa e Encontro; dá prioridade automática no próximo encontro da mesma paróquia.

## Convite
**Descrição:** Solicitação formal de participação de uma Pessoa em uma Equipe (como coordenador ou integrante), emitida pela Equipe Comando (coordenadores) ou pelo Coordenador da equipe (integrantes).
**Atributos conceituais:** pessoa convidada, equipe, papel (coordenador/integrante), status (pendente/aceito/recusado), data de envio, data de resposta, justificativa (obrigatória em caso de recusa).
**Relacionamentos:** referencia Pessoa e Equipe; recusa gera evento de notificação à Montagem e pode originar uma Substituição.

## Substituição
**Descrição:** Registro de troca de uma pessoa por outra dentro de uma equipe, motivada por recusa de convite ou indisponibilidade.
**Atributos conceituais:** pessoa que sai, pessoa que entra, equipe, motivo/justificativa, data.
**Relacionamentos:** referencia duas Pessoas e uma Equipe; preserva o histórico de ambas.

## Transferência
**Descrição:** Mudança definitiva do vínculo de uma Pessoa entre duas Paróquias.
**Atributos conceituais:** pessoa, paróquia de origem, paróquia de destino, status (pendente/aprovada/recusada), data da solicitação.
**Relacionamentos:** referencia Pessoa, Paróquia origem e destino; requer aprovação do Setorial; preserva histórico.

## Empréstimo
**Descrição:** Uso temporário de um seguidor de outra paróquia em uma equipe do encontro corrente, sem mudança definitiva de vínculo.
**Atributos conceituais:** pessoa emprestada, paróquia de origem, paróquia solicitante, equipe/vaga, status (solicitado/sugestões enviadas/adicionado), habilidade, filtros aplicados.
**Relacionamentos:** referencia Pessoa, paróquia de origem, encontro/equipe de destino; requer autorização do Setorial; fluxo em duas etapas entre perfis Montagem e Setorial.

## Solicitação (ao Setorial)
**Descrição:** Item de trabalho no inbox do Setorial, representando uma Transferência ou um Empréstimo pendente de decisão (Autorizar/Recusar).
**Relacionamentos:** referencia uma Transferência ou um Empréstimo.

## Competência (Dom)
**Descrição:** Habilidade ou dom catalogado nacionalmente (ex.: Canta, Cozinha, Violão, Fotografia, Decoração, Som, Motorista, Informática, Liturgia — 8 dons observados no catálogo).
**Responsabilidade:** ser usada como critério de composição de equipe e ranqueamento de Sugestões Inteligentes.
**Relacionamentos:** associada a muitas Pessoas; usada por Equipes como pré-requisito informal.

## Sugestão Inteligente
**Descrição:** Candidato ranqueado automaticamente para preencher uma vaga em aberto de uma Equipe, visível apenas ao perfil Montagem.
**Atributos conceituais:** pessoa candidata, score de match (0–100), composição do score em 4 fatores (histórico em equipes semelhantes 35%, competências exigidas 30%, frequência no pós-encontro 20%, cumprimento de pré-requisito 15%), tags de justificativa (ex.: "Cumpre pré-requisito", "Requer empréstimo do setor").
**Relacionamentos:** referencia Pessoa e vaga/Equipe; nunca substitui a decisão humana da Montagem.

## Lançamento Financeiro (Receita/Despesa)
**Descrição:** Movimentação financeira associada a um Encontro.
**Atributos conceituais:** tipo (receita/despesa), categoria, valor, data, situação (aprovada/pendente/paga, para despesas).
**Relacionamentos:** pertence a um Encontro; pode pertencer a uma Campanha.

## Campanha (de arrecadação)
**Descrição:** Iniciativa de arrecadação de recursos para um encontro (ex.: festa junina, venda de doces, rifa).
**Atributos conceituais:** nome, valor arrecadado, meta, status (ativa/encerrada).
**Relacionamentos:** pertence a um Encontro; agrega Receitas.

## Palestra
**Descrição:** Um dos 7 temas oficiais e numerados do encontro, em ordem fixa, com um palestrante designado.
**Relacionamentos:** pertence a um Encontro; referencia um Palestrante.

## Palestrante
**Descrição:** Pessoa responsável por ministrar uma Palestra; pode ser Convidado externo (sem cadastro de seguidor), Seguidor do movimento, ou Clero.
**Relacionamentos:** referencia zero ou uma Pessoa (quando é seguidor); associado a Palestras.

## Notificação
**Descrição:** Evento de sistema exibido no drawer de notificações (convite enviado/aceito/recusado, etc.).
**Atributos conceituais:** ícone, tipo (info/ok/danger), título, texto, tempo relativo, lida/não lida.

## Relatório
**Descrição:** Documento gerado sob demanda a partir dos dados do sistema (por tipo).
**Atributos conceituais:** tipo, data de geração.
