# Requisitos Não Funcionais

Apenas requisitos percebidos diretamente no protótipo — nenhum item foi inventado.

## Usabilidade
- **RNF-01:** o sistema deve manter navegação consistente entre perfis: mesmo padrão de sidebar (seletor de perfil, menu, rodapé) e header (breadcrumb, busca, notificações, avatar) em todas as telas internas.
- **RNF-02:** toda lista de dados deve comunicar claramente seus 4 estados possíveis quando aplicável: carregando (skeleton), com dados, vazio (com orientação de próxima ação) e erro (com ação de nova tentativa) — padrão explicitamente modelado na tela de Lista de Pessoas e que deve ser replicado às demais listas do sistema.
- **RNF-03:** ações destrutivas ou sensíveis (recusar convite, recusar transferência, registrar substituição) devem exigir confirmação/justificativa textual antes de serem efetivadas.
- **RNF-04:** fluxos longos (criação de encontro) devem ser guiados por assistente em etapas (wizard) com possibilidade de voltar, revisar e editar antes de confirmar.
- **RNF-05:** avisos de regra de negócio relevantes (ex.: limite de vagas, pré-requisito de equipe, natureza das sugestões automáticas) devem ser exibidos em contexto, no momento da decisão do usuário, não apenas em documentação externa.

## Segurança e Auditoria
- **RNF-06:** o acesso aos dados deve ser sempre restrito ao escopo hierárquico do perfil ativo (paróquia, setor, diocese ou nacional).
- **RNF-07:** ações que alteram vínculo de pessoas (transferência, empréstimo, substituição) devem ser auditáveis: o protótipo modela isso como histórico nunca apagado e notificações automáticas às partes responsáveis (Montagem).
- **RNF-08:** recusas de convite devem obrigatoriamente registrar justificativa textual, criando rastro de decisão.

## Performance
- **RNF-09:** listas grandes (ex.: 248 pessoas) devem ser paginadas em vez de carregadas integralmente.
- **RNF-10:** o sistema deve exibir feedback de carregamento (skeleton) durante buscas assíncronas de dados, evitando tela em branco.

## Responsividade
- **RNF-11:** o protótipo foi desenhado como aplicação de densidade alta voltada a desktop (sidebar fixa, grids de 4 colunas nos dashboards); não há evidência no protótipo de um layout mobile dedicado — comportamento em telas pequenas não foi especificado (ver pendências).

## Acessibilidade
- Não há requisitos de acessibilidade (contraste mínimo, navegação por teclado, leitores de tela, textos alternativos) explicitamente demonstrados ou testáveis no protótipo estático. **Não inventar valores — este item deve ser levantado à parte com o Product Owner.**

## Pendências de requisitos não funcionais
- Requisitos de desempenho quantitativos (tempo de resposta, volume de usuários simultâneos) não especificados.
- Requisitos de disponibilidade/SLA não especificados.
- Requisitos de internacionalização (o sistema hoje é 100% em português do Brasil) — não há indicação de necessidade de outros idiomas.
- Requisitos de acessibilidade formal (WCAG ou equivalente) não especificados — recomenda-se validar com o Product Owner antes da implementação.
- Requisito de responsividade mobile/tablet não especificado — o protótipo assume desktop.
