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
- Estrutura completa das telas Presenças, Frequência e Prioridades (campos, filtros, ações) não está definida no protótipo atual — **precisa ser desenhada e validada com o Product Owner antes da implementação.**
- Régua numérica exata que separa as faixas de prioridade Alta/Média/Baixa não está especificada.
- Regra de como a prioridade do Pós-Encontro se conecta operacionalmente ao módulo de Montagem/Sugestões Inteligentes (é um dos 4 fatores do score, "frequência no pós-encontro" — ver `montagem-de-equipes.md` — mas o mecanismo de leitura desse dado pelo motor de sugestão não está detalhado).
