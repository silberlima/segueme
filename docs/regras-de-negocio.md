# Regras de Negócio Consolidadas

Cada regra indica id, descrição, motivação, quando é aplicada, restrições/impactos e dependências. Regras detalhadas por módulo também aparecem nos arquivos correspondentes; esta lista consolida todas para referência cruzada.

## RN-01 — Cadastro nacional único de pessoa
**Descrição:** cada pessoa tem um único cadastro, válido em todo o sistema nacional, independentemente da paróquia a que pertence.
**Motivação:** evitar duplicidade e permitir rastrear a trajetória da pessoa em qualquer paróquia/encontro.
**Quando se aplica:** toda vez que uma pessoa é cadastrada ou referenciada (inscrição, equipe, transferência).
**Restrições:** não deve haver criação de um segundo cadastro para a mesma pessoa física.
**Impactos:** telas de Pessoas, Cadastro, Perfil, Histórico.
**Dependências:** RN-02.

## RN-02 — Histórico nunca apagado
**Descrição:** o histórico de participação de uma pessoa (equipes, encontros, transferências, empréstimos) nunca é removido ou sobrescrito.
**Motivação:** preservar a memória institucional do movimento.
**Quando se aplica:** em qualquer alteração de vínculo (transferência, empréstimo, substituição).
**Dependências:** RN-01, RN-08, RN-09.

## RN-03 — Casal = 2 cadastros vinculados
**Descrição:** um casal é sempre representado por dois cadastros de pessoa individuais, vinculados entre si.
**Motivação:** manter a granularidade do cadastro nacional mesmo para membros que atuam como casal (ex.: Equipe Dirigente).
**Quando se aplica:** telas de Casais, composição da Equipe Dirigente.

## RN-04 — Hierarquia territorial estrita
**Descrição:** Nacional → Diocese/Arquidiocese → Setor → Paróquia; cada usuário só acessa dados do seu nível e abaixo.
**Motivação:** refletir a estrutura real de governança do movimento e da Igreja.
**Quando se aplica:** em toda consulta de dados por perfil de acesso territorial.
**Dependências:** RN-05.

## RN-05 — Perfil de acesso determina menu e painel inicial
**Descrição:** o perfil ativo do usuário define exclusivamente o menu lateral e o dashboard exibido.
**Quando se aplica:** login e troca de perfil.
**Pendência associada:** atribuição de perfil(is) por usuário real (`pendencias-gerais.md`).

## RN-06 — Paróquia não cria equipes
**Descrição:** as equipes de serviço de um encontro (nome, mínimo, máximo, pré-requisito) são definidas nacionalmente; a paróquia apenas preenche vagas.
**Motivação:** padronizar a operação de encontros em todo o movimento.
**Quando se aplica:** composição de equipes de qualquer encontro.

## RN-07 — Somente a Montagem altera composição de equipes
**Descrição:** apenas o perfil Equipe Montagem pode adicionar, trocar ou remover integrantes/coordenadores de uma equipe.
**Motivação:** centralizar a responsabilidade de composição em uma única equipe funcional.
**Quando se aplica:** telas de Equipes, Sugestões Inteligentes, Substituições.
**Dependências:** RN-08.

## RN-08 — Sugestões Inteligentes exclusivas da Montagem
**Descrição:** o ranqueamento automático de candidatos só é visível ao perfil Montagem.
**Motivação:** a decisão de composição de equipe é sensível e deve ficar sob controle da equipe responsável.
**Quando se aplica:** `screen_sugestoes`, painel de Montagem, vagas em aberto em Equipes.

## RN-09 — Composição do score de match (35/30/20/15%)
**Descrição:** o score de sugestão de candidato combina histórico em equipes semelhantes (35%), competências exigidas (30%), frequência no pós-encontro (20%) e cumprimento do pré-requisito (15%).
**Motivação:** balancear experiência, aptidão, engajamento contínuo e elegibilidade formal.
**Quando se aplica:** cálculo de qualquer sugestão de candidato a vaga.
**Restrições:** a fórmula interna de cada fator não está detalhada (pendência).
**Dependências:** RN-19 (frequência no pós-encontro), RN-06 (pré-requisito da equipe).

## RN-10 — Sugestões não substituem decisão humana
**Descrição:** a indicação automática de candidato é sempre apoio; a efetivação depende de ação explícita da Montagem.
**Quando se aplica:** toda tela de Sugestões Inteligentes.

## RN-11 — Fluxo de convite em cadeia
**Descrição:** Montagem monta a equipe → Comando convida coordenador → coordenador aceita → coordenador convida integrantes.
**Motivação:** distribuir a responsabilidade de convite entre as equipes envolvidas na execução do encontro.
**Quando se aplica:** todo o ciclo de convite de uma equipe.

## RN-12 — Recusa exige justificativa e notifica a Montagem
**Descrição:** toda recusa de convite (coordenador ou integrante) exige texto de justificativa e gera notificação imediata à Montagem.
**Motivação:** garantir rastreabilidade e ação rápida de substituição.
**Quando se aplica:** telas de Convites, Recusas, Substituições.
**Dependências:** RN-13.

## RN-13 — Substituição preserva histórico de ambas as pessoas
**Descrição:** ao substituir um integrante por outro, nenhum dos dois perde seu histórico individual.
**Quando se aplica:** `screen_substituicoes`.
**Dependências:** RN-02.

## RN-14 — Funil de inscrição
**Descrição:** Pré-inscrição → Ficha (preenchida pela Equipe Fichas) → Confirmação (pessoa vira Seguimista) → 1ª participação em equipe → Seguidor.
**Motivação:** padronizar o processo de entrada de um novo participante no movimento.
**Quando se aplica:** todo o módulo de Inscrições.
**Dependências:** RN-15, RN-16, RN-01.

## RN-15 — Ficha exclusiva da Equipe Fichas
**Descrição:** somente a Equipe Fichas preenche/edita a Ficha de Inscrição.
**Quando se aplica:** `screen_ficha`.

## RN-16 — Limite de vagas → Cadastro de Reserva
**Descrição:** ao atingir o limite de vagas configurado para o encontro, novas pré-inscrições entram automaticamente no Cadastro de Reserva, em ordem de chegada, com prioridade automática no próximo encontro.
**Motivação:** gerenciar demanda superior à capacidade do encontro sem perder o candidato.
**Quando se aplica:** wizard "Criar Encontro" (passo 2) e módulo de Inscrições.

## RN-17 — Transferência e Empréstimo exigem autorização do Setorial
**Descrição:** nenhuma mudança de vínculo (definitiva ou temporária) entre paróquias ocorre sem uma decisão explícita do Setorial.
**Motivação:** manter governança territorial sobre a movimentação de pessoas entre paróquias.
**Quando se aplica:** módulos de Transferências, Empréstimos, Solicitações.
**Dependências:** RN-02.

## RN-18 — Fluxo de Empréstimo é bidirecional (Montagem ⇄ Setorial)
**Descrição:** a Montagem solicita, o Setorial seleciona e envia candidatos, a Montagem efetiva a adição.
**Quando se aplica:** `screen_emprestimos`.

## RN-19 — Prioridade pós-encontro por frequência
**Descrição:** jovens com maior frequência nos encontros de pós-encontro recebem prioridade para servir em próximas equipes.
**Motivação:** reconhecer e incentivar o engajamento contínuo após o encontro.
**Quando se aplica:** painel Pós-Encontro; é um dos 4 fatores do score de sugestão (RN-09).

## RN-20 — Equipe Dirigente com 5 pastas fixas
**Descrição:** toda Equipe Dirigente tem 5 pastas com 2 jovens + 1 casal cada, acompanhadas pelo diretor espiritual.
**Quando se aplica:** wizard "Criar Encontro" (passo 3), `screen_dirigente`.
**Restrição/pendência:** nomenclatura das 5 pastas diverge entre as telas do protótipo (ver `equipe-dirigente-e-comando.md`).

## RN-21 — Criação de encontro permite pendências
**Descrição:** o encontro pode ser criado mesmo com uma pasta da Equipe Dirigente incompleta; o sistema apenas avisa, não bloqueia.
**Quando se aplica:** wizard "Criar Encontro" (passo 5 — Revisão).

## RN-22 — Encontro tem 7 palestras oficiais em ordem fixa
**Descrição:** o conteúdo formativo do encontro é padronizado nacionalmente em 7 temas numerados e não reordenáveis.
**Quando se aplica:** `screen_palestras`.

## RN-23 — Tipos de palestrante
**Descrição:** um palestrante pode ser Convidado externo (sem cadastro de pessoa), Seguidor (com cadastro) ou Clero.
**Quando se aplica:** `screen_palestrantes`.

## RN-24 — Ciclo de situação de despesa
**Descrição:** uma despesa financeira passa pelos estados pendente → aprovada → paga.
**Quando se aplica:** `screen_despesas`.
**Pendência:** aprovador não identificado.

## RN-25 — Notificações têm estado lida/não lida e tipo
**Descrição:** cada notificação tem um tipo (informativa/ok/perigo) e um estado de leitura, refletido em contador de badge.
**Quando se aplica:** drawer de notificações, badges de menu.
