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
