# Dashboards por Perfil

## 1. Objetivo
Fornecer, para cada um dos 9 perfis operacionais, uma visão consolidada (KPIs + destaques) do seu escopo de responsabilidade ao entrar no sistema.

## 2. Padrão comum a todos os dashboards
- Cabeçalho com título do painel, subtítulo de escopo (paróquia/setor/diocese/encontro) e ação(ões) principal(is) em botão(ões) no topo direito.
- Faixa de 4 indicadores-chave (KPI cards): rótulo, ícone, valor numérico grande, variação ou nota contextual.
- Corpo em duas colunas com listas, gráficos de barra simples e cartões de destaque (pendências, progresso, financeiro).
- Nenhum dashboard tem paginação; todos mostram um recorte "recente/top N" com atalho "Ver todos"/"Ver no mapa" para a lista completa.

## 3. Telas por perfil

### 3.1 Painel Nacional (`dash_nacional`)
- **Quem acessa:** perfil Nacional.
- **KPIs:** Seguidores no Brasil, Dioceses, Paróquias, Encontros no ano.
- **Conteúdo:** gráfico de barras "Encontros por região" (Nordeste, Sudeste, Sul, Centro-Oeste, Norte); ranking "Dioceses em destaque" (paróquias e seguidores por diocese).
- **Ações:** "Relatório anual" (exportação).

### 3.2 Painel da Diocese (`dash_diocese`)
- **Quem acessa:** perfil Diocese.
- **KPIs:** Seguidores, Setores, Paróquias, Encontros no ano.
- **Conteúdo:** gráfico de barras "Seguidores por setor"; cartão "Solicitações ao setorial" (contagem de transferências/empréstimos/aprovações de encontro).
- **Ações:** "Relatório".

### 3.3 Painel do Setor (`dash_setorial`)
- **Quem acessa:** perfil Setorial.
- **KPIs:** Solicitações pendentes, Transferências no mês, Empréstimos ativos, Seguidores no setor.
- **Conteúdo:** lista "Solicitações pendentes de aprovação" com card por item (tipo, tempo, descrição, botões Aprovar/Autorizar e Recusar); lista "Paróquias do setor" com contagem de seguidores e status do próximo encontro.
- **Ações:** Aprovar/Autorizar, Recusar (por item de solicitação); "Relatório".
- **Regra de negócio:** aprovação/recusa aqui é a mesma ação central usada pela tela `screen_solicitacoes` (inbox dedicado) — ver `transferencias-e-emprestimos.md`.

### 3.4 Painel da Paróquia (`dash_paroquia`)
- **Quem acessa:** perfil Paróquia.
- **KPIs:** Seguidores ativos, Encontros realizados, Próximo encontro (dias), Pendências.
- **Conteúdo:** card do próximo encontro (status "Em preparação", datas, local, diretor espiritual, progresso de "Equipes montadas" e "Inscrições confirmadas", checklist de equipes-chave completas); tabela "Encontros recentes"; lista de "Pendências" (equipes incompletas, cadastro reserva, fichas a preencher); resumo "Financeiro do encontro" com saldo e barra de progresso por campanha.
- **Ações:** "Exportar", "Novo encontro" (abre wizard `screen_criar_encontro`).

### 3.5 Painel de Fichas (`dash_fichas`)
- **Quem acessa:** perfil Equipe Fichas.
- **KPIs:** Pré-inscrições (aguardando ficha), Confirmados, Vagas restantes, Cadastro reserva.
- **Conteúdo:** funil visual "Fluxo de inscrição" (Pré-inscrição → Em análise → Confirmados → Seguimistas → Seguidores); lista "Pré-inscrições aguardando ficha" com botão "Preencher"; lista "Cadastro de reserva" ordenada por prioridade.
- **Ações:** "Nova ficha".

### 3.6 Painel de Montagem (`dash_montagem`)
- **Quem acessa:** perfil Equipe Montagem.
- **KPIs:** Equipes incompletas, Vagas abertas, Sugestões disponíveis, Recusas.
- **Conteúdo:** lista "Equipes do encontro" com barra de status (completa/incompleta, contagem atual/máxima); painel "Sugestões inteligentes" para a equipe com maior prioridade, com até 3 candidatos ranqueados (score, barra de match, tags, botões "Adicionar à equipe"/"Perfil").
- **Ações:** "Substituição" (atalho para `screen_substituicoes`), "Confirmar montagem".
- **Regra de negócio:** este painel é o único, junto com `screen_equipes` e `screen_sugestoes`, que exibe sugestões inteligentes — exclusivo do perfil Montagem.

### 3.7 Painel de Comando (`dash_comando`)
- **Quem acessa:** perfil Equipe Comando.
- **KPIs:** Convites enviados, Aceites, Pendentes, Recusas.
- **Conteúdo:** lista "Acompanhamento de convites" (pessoa, equipe/papel, status); gráfico de barras "Aceites por equipe" (aceitos/convidados).
- **Ações:** "Novo convite".

### 3.8 Painel Financeiro (`dash_financas`)
- **Quem acessa:** perfil Equipe Finanças.
- **KPIs:** Saldo atual, Receitas, Despesas, Campanhas ativas.
- **Conteúdo:** lista "Campanhas de arrecadação" com barra de progresso até a meta; lista "Lançamentos recentes" (receita/despesa, categoria, data, valor com sinal).
- **Ações:** "Extrato" (exportação), "Lançamento" (novo).

### 3.9 Painel Pós-Encontro (`dash_posencontro`)
- **Quem acessa:** perfil Pós-Encontro.
- **KPIs:** Jovens acompanhados, Presença média, Encontros pós no mês, Prioridade próximo (contagem de elegíveis).
- **Conteúdo:** gráfico de barras "Frequência nos encontros pós" (mensal); cartões de próximo encontro pós e confirmados; ranking "Prioridade para próximos encontros" (por nº de presenças, classificado em Alta/Média/Baixa).
- **Ações:** "Registrar presença".
- **Nota de escopo:** este dashboard existe e está pronto, mas as telas de detalhe do módulo (Presenças, Frequência, Prioridades) estão fora do mapa de navegação oficial no momento — ver `pos-encontro.md` e `pendencias-gerais.md`.

## 4. Regras de negócio
- **RN-DASH-01:** cada perfil só visualiza o dashboard mapeado para ele; não há navegação direta entre dashboards de perfis diferentes exceto pela troca de perfil.
- **RN-DASH-02:** indicadores de pendência exibidos nos dashboards (ex.: "3 equipes incompletas", "4 solicitações pendentes") também aparecem como badges no menu lateral correspondente.

## 5. Pendências
- Os valores numéricos dos KPIs são estáticos/ilustrativos no protótipo; regras de cálculo exatas (período de apuração, corte de data, etc.) precisam ser validadas com o Product Owner.
- Botões "Exportar"/"Relatório"/"Extrato" não têm formato de saída definido (PDF, planilha, etc.) — ver `sistema-relatorios-config.md`.

## 6. Decisões da implementação (Fase 12) — sujeitas a validação do PO

Registro do que a Fase 12 adotou onde o protótipo não determina a regra. Tudo aqui é melhor esforço documentado, não decisão fechada.

### 6.1 Período de apuração dos KPIs
Concentrado num único lugar (`PeriodoApuracao`, módulo `dashboards`) para que a revisão com o PO troque a regra em um ponto, não em nove painéis:

- **"no ano"** = ano civil corrente (1º de janeiro a 31 de dezembro), pela data de início do Encontro.
- **"no mês"** = mês civil corrente, pela data de solicitação (transferências) ou pela data do encontro pós.
- **Gráfico mensal do Pós-Encontro** = 6 meses retroativos incluindo o corrente; meses sem encontro aparecem zerados, para o gráfico não mudar de largura.
- Nenhum KPI conta pela data de conclusão do fato, sempre pela de início.

### 6.2 Escopo e recortes
- Nenhum endpoint de painel recebe território por parâmetro: o escopo vem das claims do usuário autenticado (RN-04). Um perfil não consegue pedir o painel de outra diocese, setor ou paróquia, e cada rota é restrita ao seu perfil (RN-DASH-01).
- Os painéis das equipes (Fichas, Montagem, Comando, Finanças) não têm seletor de Encontro no protótipo: falam do **Encontro corrente** da paróquia — o próximo Encontro ativo que ainda não terminou e, na falta dele, o último realizado. Paróquia sem Encontro nenhum devolve o painel zerado com a ação "Novo encontro", nunca erro.
- **"Solicitações ao setorial"** (Painel da Diocese) conta transferências pendentes e empréstimos em aberto. As "aprovações de encontro" citadas no protótipo ficam de fora porque esse fluxo não existe no modelo (pendência #5).
- **"Equipe com maior prioridade"** (Painel de Montagem) = a de mais vagas abertas, com empate resolvido pelo nome para o painel não trocar de equipe entre duas cargas iguais.
- **Checklist de "equipes-chave"** (Painel da Paróquia): o protótipo não define quais equipes são chave, então o painel lista todas as equipes do Encontro e marca as completas, sem eleger um subconjunto.
- **"Confirmados" do próximo encontro pós** (Painel Pós-Encontro) é exibido como *chamada registrada*: não existe confirmação prévia de presença no modelo, apenas a chamada — que pode estar em branco.
- As etapas "Seguimistas" e "Seguidores" do funil de Fichas vêm da situação das pessoas da paróquia, não do funil do Encontro (onde `seguidores` segue sendo o placeholder da Fase 5).

### 6.3 Região da diocese
O gráfico "Encontros por região" exigiu um atributo que a hierarquia não tinha: `Diocese.regiao` (as cinco regiões do IBGE), **opcional** — não há tela de cadastro da hierarquia (pendência #25), então dioceses sem região informada são agrupadas no painel como "Não informada".

### 6.4 Badges do menu (RN-DASH-02)
O badge fica na rota que leva ao trabalho pendente — "Solicitações (inbox)" para o Setorial. Quando a tela de detalhe não é alcançável pelo menu (as que exigem um Encontro na URL), o badge fica no próprio item do painel: pendências da paróquia, fichas aguardando, equipes incompletas e convites pendentes.

### 6.5 Ações sem destino definido
"Relatório anual", "Exportar" e "Extrato" não têm formato de saída definido (Fase 13): no lugar de um botão inerte, os painéis levam à tela existente correspondente (dioceses, financeiro do Encontro, inbox de solicitações).
