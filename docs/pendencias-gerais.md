# Pendências Gerais — Decisões a Validar com o Responsável pelo Produto

Consolidação de tudo que não pôde ser determinado apenas pelo protótipo. Cada item também aparece na seção de pendências do arquivo funcional correspondente.

## Regras não especificadas
1. Regras de validação de login (formato de e-mail, política de senha, bloqueio por tentativas).
2. Fluxo completo de redefinição de senha (tela de nova senha, expiração do link de recuperação).
3. Regra de atribuição de perfil(is) de acesso a um usuário real (hoje o protótipo permite trocar de perfil livremente para fins de demonstração).
4. Regra de permitir ou não mais de um encontro ativo simultâneo por paróquia.
5. Etapa de aprovação de criação de encontro em nível de Diocese/Setorial (mencionada indiretamente no Painel da Diocese — "3 aprovações de encontro aguardando a diocese" — mas não modelada no wizard). *(Fase 12: o cartão "Solicitações ao setorial" conta apenas transferências pendentes e empréstimos em aberto, já que não existe fluxo de aprovação de Encontro — ver `dashboards-por-perfil.md` 6.2.)*
6. Fórmula exata de cálculo de cada um dos 4 fatores do score de Sugestões Inteligentes (apenas os pesos — 35/30/20/15% — estão definidos).
7. Regra de bloqueio (ou não) da ação "Confirmar montagem" quando há equipes incompletas.
8. Prazo/expiração de convites pendentes sem resposta; regras de reenvio.
9. Quem inicia o gatilho de uma solicitação de Transferência (a própria pessoa, a paróquia de origem, ou a de destino).
10. Prazo de vigência de um Empréstimo (encerramento automático ao fim do encontro?).
11. Limite de quantos empréstimos uma paróquia pode conceder simultaneamente.
12. Quem aprova uma Despesa financeira (perfil aprovador não identificado no protótipo).
13. Campos e regras de validação do formulário de "Lançamento" financeiro (categorias, forma de pagamento).
14. Régua numérica exata que separa as faixas de prioridade Alta/Média/Baixa no Pós-Encontro. *(Fase 11: default parametrizável de 75%/40% em `app.pos-encontro.regua.*`, aguardando validação — ver `pos-encontro.md` 6.4.)*
15. Lista definitiva e ementa dos 7 temas oficiais de palestra.
15-sistema. Tipos exatos dos 6 relatórios da tela Relatórios (o protótipo mostra seis cartões sem nomeá-los). *(Fase 13: um tipo por módulo de dados já implementado — Pessoas, Encontros, Inscrições, Equipes e convites, Financeiro, Pós-encontro — com os perfis autorizados em `TipoRelatorio`; ver `sistema-relatorios-config.md` 6.1.)*
16. Lista definitiva das 8 competências do catálogo nacional.

## Campos com comportamento indefinido
17. Regras de máscara/validação de telefone, data de nascimento e demais campos da Ficha de Inscrição e do Cadastro de Pessoa.
18. Regra de deduplicação de cadastro de pessoa (evitar duplicidade da mesma pessoa física).
19. Campos mínimos exigidos no primeiro contato de "Nova pré-inscrição" (o protótipo mostra apenas o botão, sem abrir o formulário correspondente).

## Fluxos incompletos
20. Telas de detalhe do módulo Pós-Encontro (`screen_presencas`, `screen_frequencia`, `screen_prioridades`) não estão desenhadas no protótipo atual — apenas referenciadas no menu e no briefing original. *(Fase 11: desenho proposto e implementado, aguardando validação — ver `pos-encontro.md` 6.2 a 6.4.)*
21. Telas de Sistema "Aprovações" (`screen_aprovacoes`) e "Pendências" (`screen_pendencias`) citadas no briefing original, mas fora do escopo já desenhado do protótipo.
22. O wizard "Criar Encontro" é navegável, mas não persiste dados reais entre sessões — o comportamento de persistência definitivo depende da futura implementação.
23. Fluxo de edição de uma pessoa já cadastrada (o protótipo modela apenas a criação).
24. Fluxo de edição/cancelamento de um encontro já criado.
25. Fluxo de cadastro/edição de uma nova Diocese, Setor ou Paróquia (apenas telas de consulta foram encontradas).
26. Conteúdo completo da tela "Detalhes do Encontro" além da aba "equipes" observada por padrão.
27. Fluxo de cadastro de um Palestrante "Convidado externo" (sem gerar cadastro de Pessoa).

## Validações ausentes
28. Mensagens de erro e sucesso do Login e da Recuperação de Senha não estão modeladas (diferente da Lista de Pessoas, que modela explicitamente estados de erro/vazio/carregando).
29. Mensagens de erro/sucesso do fluxo de confirmação de ficha de inscrição.

## Permissões não identificadas
30. Quais perfis podem **criar/editar** pessoas versus apenas visualizar.
31. Quem pode editar a designação de palestrante (Paróquia, pasta Palestras da Equipe Dirigente, ou ambos).
32. Quem pode editar a composição da Equipe Dirigente e da Equipe Comando após a criação do encontro.
33. Se "Coordenador de equipe" é um perfil de acesso à parte ou um papel dentro de outro perfil existente (não consta na lista de 10 perfis trocáveis do protótipo).
34. Quem pode alterar preferências de Notificações de outros usuários (individual vs. administração global). *(Fase 13: só as próprias — o `usuarioId` vem sempre do `sub` do token e não existe caminho de administração global; ver `sistema-relatorios-config.md` 6.4.)*
39. **Vínculo entre usuário autenticado (Keycloak) e Pessoa não existe.** Nenhuma entidade do backend liga o `sub` do JWT a um registro de `Pessoa`. Isso impede, na Fase 7, restringir de verdade o Painel do Coordenador (`screen_coordenador`) a quem aceitou o convite — hoje as ações de emitir/aceitar/recusar/reenviar convite são executadas por staff com role `EQUIPE_COMANDO` em nome da pessoa convidada (mesmo padrão já usado pela Equipe Montagem desde a Fase 6 em `AdicionarIntegranteUseCase`), nunca pelo próprio convidado logado. Pelo mesmo motivo, a notificação de recusa (RF-038/RN-12) não consegue alcançar "a Equipe Montagem" como um grupo — ela é endereçada a quem emitiu o convite (`IntegranteEquipe.createdBy`), resolvido via `ConviteRecusadoEvent`. Revisitar quando existir vínculo Pessoa↔Usuário ou um diretório de usuários por role.

## Integrações externas não detalhadas
35. Envio de e-mail de recuperação de senha (mecanismo externo não modelado).
36. Notificação formal às pessoas envolvidas em Transferência/Empréstimo fora do sistema (e-mail, SMS) — apenas a notificação interna (drawer) é observada.
37. Formato de saída de relatórios e exportações ("Exportar", "Extrato", "Relatório anual") — PDF, planilha ou outro formato não especificado. *(Fase 13: CSV no MVP; o gerador devolve colunas e linhas, então trocar o formato é escrever outro serializador — ver `sistema-relatorios-config.md` 6.3.)*

## Inconsistência a resolver
38. **Nomenclatura da Equipe Dirigente diverge entre telas:** o wizard de criação usa "Finanças, Fichas, Palestras, Pós-Encontro, Montagem"; a tela de consulta `screen_dirigente` usa "Coordenação Geral, Formação, Liturgia, Secretaria, Logística". Necessário alinhar com o Product Owner antes da implementação.

## Requisitos não funcionais em aberto
39. Requisitos de desempenho, disponibilidade/SLA, volume de usuários simultâneos.
40. Requisitos de acessibilidade formal (WCAG ou equivalente).
41. Requisito de responsividade mobile/tablet (protótipo assume desktop).
