# Pós-Encontro

## 1. Objetivo
Acompanhar a perseverança e frequência dos jovens participantes após o Encontro, priorizando quem deve ser convidado a servir em equipes nos próximos encontros.

## 2. Telas envolvidas

### 2.1 Painel Pós-Encontro (`dash_posencontro`)
Documentado em `dashboards-por-perfil.md` (seção 3.9). Painel inicial do perfil **Pós-Encontro**, com KPIs de jovens acompanhados, presença média, encontros pós no mês e prioridade para o próximo encontro; gráfico de frequência mensal; ranking de prioridade por número de presenças (classificado em Alta/Média/Baixa).

### 2.2 Telas de detalhe (referenciadas no menu do perfil, fora do mapa de navegação oficial no momento da análise)
- **Presenças (`screen_presencas`)** — registrar presença dos jovens nos encontros pós-encontro semanais/periódicos.
- **Frequência (`screen_frequencia`)** — histórico detalhado de frequência por pessoa/período.
- **Prioridades (`screen_prioridades`)** — lista detalhada de jovens priorizados para a próxima equipe, com critério de frequência.

> **Nota de escopo:** estas três telas de detalhe constam no menu lateral do perfil Pós-Encontro e no briefing original do produto, mas — no estado do protótipo analisado — não estão presentes no catálogo de telas prontas do Mapa de Navegação (`groups`). Seu comportamento detalhado (campos, validações, ações) não pôde ser extraído do protótipo e depende de nova rodada de design. Ver `pendencias-gerais.md`.

## 3. Regras de negócio (extraídas do painel e do briefing)
- **RN-POS-01:** a frequência nos encontros pós-encontro é registrada por pessoa e por data de encontro pós.
- **RN-POS-02:** quem participa com frequência tem **prioridade** para ser convidado a servir em equipes nos próximos encontros (mensagem exibida na tela: "Quem participa com frequência tem prioridade para trabalhar nas equipes").
- **RN-POS-03:** a prioridade é classificada em faixas (observadas: Alta, Média, Baixa), aparentemente por proporção de presenças (ex.: 12/12 = Alta; 3/12 = Baixa) — a régua exata de corte entre faixas não está especificada.

## 4. Casos de uso
- **UC — Registrar presença:** Ator: perfil Pós-Encontro. Fluxo principal: abrir Painel Pós-Encontro → "Registrar presença" → marcar presentes no encontro pós do dia. Pós-condição: frequência atualizada, afetando a prioridade da pessoa.

## 5. Regras pendentes de validação
- Estrutura completa das telas Presenças, Frequência e Prioridades (campos, filtros, ações) não está definida no protótipo atual — **precisa ser desenhada e validada com o Product Owner antes da implementação.** Desenho adotado na Fase 11 documentado na seção 6, ainda **sujeito a validação do PO**.
- Régua numérica exata que separa as faixas de prioridade Alta/Média/Baixa não está especificada. Default parametrizável adotado na Fase 11: ver seção 6.4.
- Regra de como a prioridade do Pós-Encontro se conecta operacionalmente ao módulo de Montagem/Sugestões Inteligentes (é um dos 4 fatores do score, "frequência no pós-encontro" — ver `montagem-de-equipes.md` — mas o mecanismo de leitura desse dado pelo motor de sugestão não está detalhado). Mecanismo implementado na Fase 11: ver seção 6.5.

## 6. Desenho adotado na Fase 11 (a validar com o PO)
Como as 3 telas de detalhe não existiam no protótipo (pendência #20), a Fase 11 incluiu a sub-etapa de design prevista no `plano-implementacao.md`. O que foi implementado está registrado aqui para servir de base à validação — **nada nesta seção substitui a decisão do PO**.

### 6.1 Modelo de dados
- **`EncontroPos`** (paróquia, data, tema opcional): o encontro pós em si, único por paróquia e data. O plano citava só a frequência com a data embutida; a data virou entidade porque o módulo precisa **agendar** o próximo encontro e **contar quantos houve** no período — esse total é o denominador das faixas (ver 6.4).
- **`FrequenciaPosEncontro`** (encontro pós, pessoa, presente): no máximo um registro por pessoa e encontro pós; remarcar atualiza o registro em vez de acumular linhas.
- **Pool de acompanhados:** pessoas da paróquia com situação `SEGUIMISTA` ou `SEGUIDOR` — quem já fez o Encontro. Quem virou Seguidor permanece no acompanhamento para que o fator de frequência da Fase 6 tenha dado sobre ele.

### 6.2 Presenças (`screen_presencas`)
Seleção de paróquia e encontro pós; card "Agendar encontro pós" (data + tema) para os perfis Pós-Encontro e Paróquia; lista de chamada com todo o pool de acompanhados e botões Presente/Ausente por jovem, salva **em lote** (uma requisição por chamada, não por jovem). Quem nunca foi conferido aparece como **"Não conferido"**, estado distinto de "Ausente".

### 6.3 Frequência (`screen_frequencia`)
Filtros de paróquia e período (de/até; sem filtro, apura do início dos registros até hoje). Uma linha por acompanhado com presenças/total e barra de frequência colorida pela faixa. Quem nunca teve presença registrada aparece com 0 em vez de sumir da lista.

### 6.4 Prioridades (`screen_prioridades`) e a régua
Ranking acumulado (sem recorte de período) da maior para a menor frequência, com posição, presenças/total, barra e a faixa como badge; empate resolvido por nome. Exibe a mensagem de RN-POS-02.

- **Denominador:** total de encontros pós **realizados pela paróquia** no período, não o número de registros da pessoa — é o que reproduz os exemplos de RN-POS-03 (3/12 = Baixa). Quem faltou sem registro conta como ausente.
- **Régua default (a validar):** `>= 75%` Alta, `>= 40%` Média, abaixo disso Baixa. Escolhida por coerência com os exemplos do próprio doc (12/12 = Alta, 3/12 = Baixa).
- **Parametrizável, não hardcoded:** configurada em `app.pos-encontro.regua.*` (`application.yml`), ajustável por ambiente sem alterar código. Quando a Fase 13 (Configurações) entregar parâmetros persistidos, a régua é candidata natural a migrar para lá.
- Encontros pós agendados para datas futuras nunca entram no cálculo — ainda não aconteceram.

### 6.5 Conexão com a Montagem (Fase 6)
O motor de sugestões consome uma consulta agregada única (`ConsultarPercentualFrequenciaUseCase`) que devolve o percentual de frequência por pessoa, evitando N+1 no pool nacional de candidatos. O fator "frequência no pós-encontro" (20% do score) deixou de ser placeholder — `SuggestionEngine` passou à versão `v2-frequencia-posencontro`.

**Ausência de dado ≠ 0% de frequência:** quem não tem nenhum registro de pós-encontro não pontua no fator, mas também não é penalizado como faltoso, para não prejudicar candidatos de paróquias que ainda não fazem acompanhamento. Essa distinção é uma decisão de implementação e também merece validação do PO.
