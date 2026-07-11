# Documentação Funcional — Sistema Nacional Segue-me

Documentação gerada a partir da análise do protótipo navegável de alta fidelidade (`Segue-me.dc.html`). Cobre exclusivamente comportamento de sistema, regras de negócio e requisitos funcionais — sem definição de tecnologia, linguagem ou banco de dados.

## Como esta documentação está organizada

Cada arquivo abaixo é independente e cobre uma funcionalidade ou grupo de telas relacionado, conforme a arquitetura de informação observada no protótipo (objeto `groups` do mapa de navegação).

1. `autenticacao-acesso.md` — Login, recuperação de senha, perfis de acesso, mapa de navegação, shell (sidebar/header/busca/notificações)
2. `dashboards-por-perfil.md` — Os 9 painéis (Nacional, Diocese, Setorial, Paróquia, Fichas, Montagem, Comando, Finanças, Pós-Encontro)
3. `cadastro-de-pessoas.md` — Lista de Pessoas, Cadastro, Perfil Completo, Histórico, Casais, Competências (catálogo)
4. `gerenciamento-de-encontros.md` — Lista de Encontros, Wizard "Criar Encontro", Detalhes do Encontro
5. `equipe-dirigente-e-comando.md` — Equipe Dirigente (5 pastas), Equipe Comando
6. `inscricoes.md` — Pré-inscrição, Ficha de Inscrição, Cadastro de Reserva, Participantes
7. `montagem-de-equipes.md` — Workspace de Equipes, Sugestões Inteligentes, Substituições
8. `convites-aceites-recusas.md` — Convites, Aceites, Recusas, Coordenador, Equipe (detalhe individual)
9. `transferencias-e-emprestimos.md` — Transferências entre paróquias, Empréstimos de seguidores, Solicitações (Setorial)
10. `financeiro.md` — Financeiro (visão geral), Receitas, Despesas, Campanhas
11. `conteudo-palestras.md` — Palestras, Palestrantes
12. `pos-encontro.md` — Painel Pós-Encontro e telas relacionadas (fora do mapa navegável ativo — ver pendências)
13. `hierarquia-territorial.md` — Dioceses, Paróquias (visão hierárquica)
14. `sistema-relatorios-config.md` — Relatórios, Notificações, Configurações
15. `entidades-de-dominio.md` — Modelo conceitual do domínio (sem banco de dados)
16. `regras-de-negocio.md` — Regras de negócio consolidadas (RN-001…)
17. `requisitos-funcionais.md` — Requisitos funcionais consolidados (RF-001…)
18. `requisitos-nao-funcionais.md` — Requisitos não funcionais percebidos no protótipo
19. `casos-de-uso.md` — Casos de uso consolidados (UC-01…)
20. `pendencias-gerais.md` — Decisões e regras não determináveis apenas pelo protótipo

## Visão geral do sistema

O Segue-me é um ERP do Movimento Segue-me (movimento de jovens da Igreja Católica), organizado em uma hierarquia territorial: **Nacional → Arquidiocese/Diocese → Setorial → Paróquia**. A unidade central de operação é o **Encontro** — evento realizado por uma paróquia, composto por equipes de serviço (equipistas) que atendem aos participantes (seguidores/seguimistas em formação).

O sistema é acessado por **10 perfis** distintos (perfil de acesso trocável no protótipo para fins de demonstração — ver `autenticacao-acesso.md`), cada um com seu próprio painel inicial e menu lateral, refletindo a hierarquia e as equipes funcionais de um encontro.

### Regras estruturais fundamentais (citadas em todo o protótipo)
- Hierarquia territorial: Nacional → Diocese → Setorial → Paróquia. Cada usuário só acessa o que sua posição na hierarquia permite.
- Pessoa é um cadastro nacional único; seu histórico nunca é apagado, mesmo com transferências entre paróquias.
- Casal = 2 cadastros de pessoa vinculados.
- Cada paróquia tem seus próprios encontros, equipes e seguidores; apenas o cadastro de pessoas é compartilhado nacionalmente.
- Equipes de um encontro são definidas nacionalmente (nome, mínimo/máximo de integrantes, pré-requisitos); a paróquia não cria equipes novas.
- Somente a Equipe Montagem altera a composição das equipes de um encontro; sugestões inteligentes de candidatos só são visíveis para esse perfil.
- Fluxo de convite de equipe: Montagem monta a equipe → Comando convida o coordenador → coordenador aceita → coordenador convida integrantes. Recusa exige justificativa e notifica a Montagem.
- Fluxo de inscrição: Pré-inscrição → preenchimento de Ficha (pela Equipe Fichas) → Confirmação (pessoa vira "Seguimista") → participação na 1ª equipe → "Seguidor". Ao esgotar vagas, novas pré-inscrições entram no Cadastro de Reserva com prioridade no próximo encontro.
- Empréstimo (uso temporário de um seguidor de outra paróquia) e Transferência (mudança definitiva de paróquia) sempre exigem autorização do Setorial; o histórico da pessoa é sempre preservado.
