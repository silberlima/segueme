# Autenticação, Acesso e Shell do Sistema

## 1. Objetivo
Permitir que os usuários entrem no sistema, recuperem senha, e naveguem entre perfis, módulos e telas através da estrutura de shell (sidebar, header, busca global, notificações).

## 2. Telas envolvidas

### 2.1 Login
- **Objetivo:** autenticar o usuário no sistema.
- **Quem pode acessar:** qualquer usuário não autenticado.
- **Componentes:** painel institucional (marca, mensagem de boas-vindas), formulário de acesso.
- **Campos:**
  - E-mail (texto, obrigatório, formato e-mail) — sem máscara, validação de formato pendente de definição.
  - Senha (texto mascarado/password, obrigatório).
- **Ações/botões:** "Entrar" (submete login), "Esqueci a senha" (navega para Recuperar Senha).
- **Estados:** apenas estado padrão observado no protótipo; estados de erro de credenciais e de carregamento não estão representados.
- **Mensagens:** nenhuma mensagem de erro/sucesso está modelada no protótipo (ver Pendências).
- **Regras de negócio:** acesso restrito a credenciais do movimento; texto de ajuda orienta contatar o coordenador da paróquia ou o setorial em caso de dificuldade de acesso — sem fluxo de autoatendimento além da recuperação de senha.

### 2.2 Recuperar Senha (`screen_recuperar`)
- **Objetivo:** permitir que o usuário solicite redefinição de senha.
- **Quem pode acessar:** qualquer usuário não autenticado (chegando pelo link "Esqueci a senha" do Login).
- **Campos:** E-mail cadastrado (texto, obrigatório).
- **Ações:** "Enviar link de recuperação", "Voltar ao login".
- **Regras de negócio:** o link de recuperação é enviado ao e-mail cadastrado no movimento; não há tela de redefinição de senha propriamente dita no protótipo (ver Pendências).
- **Fluxo de saída:** volta ao Login após o envio (comportamento pós-envio — mensagem de confirmação — não está detalhado no protótipo).

### 2.3 Mapa de Navegação (`map`)
- **Objetivo:** apresentar a arquitetura de informação completa do sistema, permitindo abrir qualquer tela a partir de um catálogo visual.
- **Quem pode acessar:** todos os perfis autenticados (link "Mapa de navegação" fixo no rodapé da sidebar).
- **Descrição funcional:** lista todas as telas do sistema agrupadas por módulo (Acesso, Dashboards, Pessoas, Encontros, Equipes & Convites, Inscrições, Conteúdo, Finanças, Sistema). Cada cartão mostra ícone, nome e uma tag de status: "Pronto para navegar" (implementado) ou "Previsto no escopo" (ainda não implementado).
- **Ações:** clique no cartão abre a tela correspondente.
- **Estado atual:** todas as telas do mapa estão marcadas como prontas, exceto o wizard de criação de encontro (parcial — sem persistência real) e o módulo Pós-Encontro/algumas telas de Sistema, que existem fora do mapa oficial (ver `pos-encontro.md` e pendências).

### 2.4 Shell do aplicativo (sidebar, header, busca, notificações)
Presente em todas as telas internas (exceto Login e Recuperar Senha).

**Sidebar:**
- Seletor de perfil de acesso no topo: dropdown com os 10 perfis disponíveis (ver seção 3). Ao trocar, o menu lateral e o painel inicial mudam de acordo com o perfil selecionado.
- Menu de navegação: lista de itens específicos do perfil ativo (ver `menus` por perfil na seção 3), com badges numéricos de pendência em alguns itens (ex.: "Solicitações · 4", "Pré-inscrições · 7").
- Rodapé: link "Mapa de navegação", cartão do usuário logado (nome, perfil atual) e botão "Sair" (retorna ao Login).

**Header:**
- Breadcrumb: perfil atual › título da tela atual.
- Busca global (atalho ⌘K): campo de busca com dropdown de resultados agrupados por tipo (Pessoas, Encontros). Foco abre o dropdown; clique num resultado navega à tela correspondente.
- Sino de notificações: abre drawer lateral com lista de notificações (convite enviado/aceito/recusado etc.), contador de não lidas.
- Avatar do usuário logado.

## 3. Perfis de acesso (quem pode acessar o quê)

O sistema define **10 perfis**, cada um com painel inicial (`dash`) e menu lateral próprios:

| Perfil | Escopo | Painel inicial |
|---|---|---|
| Nacional | Brasil · visão consolidada | Painel Nacional |
| Diocese | Uma Arquidiocese/Diocese | Painel da Diocese |
| Setorial | Um Setor dentro de uma Diocese | Painel do Setor |
| Paróquia | Uma Paróquia | Painel da Paróquia |
| Equipe Fichas | Um Encontro | Painel de Fichas |
| Equipe Montagem | Um Encontro | Painel de Montagem |
| Equipe Comando | Um Encontro | Painel de Comando |
| Equipe Finanças | Um Encontro | Painel Financeiro |
| Pós-Encontro | Uma Paróquia | Painel Pós-Encontro |
| Seguidor | Perfil pessoal | Perfil Completo (próprio) |

Cada perfil vê apenas os itens de menu listados para ele (ex.: só Setorial vê "Solicitações"; só Montagem vê "Sugestões" e o botão de sugestões inteligentes dentro de Equipes; só Seguidor vê Perfil/Histórico próprios). **Regra de acesso:** o mapeamento perfil → menu → telas é a fonte de verdade de permissão de navegação observada no protótipo; não há, porém, um mecanismo de permissão em nível de ação (ex.: campo editável apenas por certo perfil) modelado explicitamente além do observado em Equipes (ver `montagem-de-equipes.md`).

> **Pendente de validação:** o protótipo permite trocar de perfil livremente pelo seletor da sidebar, para fins de demonstração. Em produção, presume-se que cada usuário real tenha um único perfil (ou um conjunto fixo) atribuído por login — este mapeamento usuário→perfil(is) permitido(s) não está definido e precisa ser validado com o responsável pelo produto.

## 4. Fluxos

### Fluxo: Login
- **Objetivo:** acessar o sistema.
- **Pré-condição:** usuário possui credencial cadastrada no movimento.
- **Cenário principal:** usuário informa e-mail e senha → clica "Entrar" → sistema autentica → usuário é levado ao Mapa de Navegação (comportamento observado no protótipo: login sempre leva ao mapa, independente do perfil).
- **Fluxos alternativos:** clique em "Esqueci a senha" leva à tela de recuperação.
- **Exceções:** credencial inválida — mensagem e tratamento não definidos no protótipo (pendência).
- **Pós-condição:** sessão iniciada; sidebar/header exibidos.

### Fluxo: Recuperação de senha
- **Objetivo:** obter novo acesso.
- **Pré-condição:** usuário possui e-mail cadastrado.
- **Cenário principal:** usuário informa e-mail → clica "Enviar link de recuperação" → sistema envia e-mail (comportamento externo não modelado) → usuário retorna ao login.
- **Exceções:** e-mail não cadastrado — tratamento não definido (pendência).

### Fluxo: Troca de perfil
- **Objetivo:** mudar o contexto de navegação para outro perfil.
- **Cenário principal:** usuário abre o seletor de perfil na sidebar → escolhe um perfil da lista → sistema navega ao painel inicial daquele perfil e atualiza o menu lateral.

### Fluxo: Busca global
- **Objetivo:** localizar rapidamente uma pessoa ou um encontro.
- **Cenário principal:** usuário foca o campo de busca → dropdown mostra resultados agrupados → usuário clica em um resultado → sistema navega à tela de detalhe correspondente (perfil da pessoa, ou detalhe do encontro).
- **Pendente de validação:** comportamento de busca com termo digitado (filtragem real dos resultados) não está implementado no protótipo — hoje mostra sempre a mesma lista fixa de exemplos.

## 5. Regras de negócio
- **RN-ACESSO-01:** O acesso ao sistema respeita a hierarquia territorial; cada perfil só enxerga dados de seu próprio escopo (paróquia, setor, diocese ou nacional).
- **RN-ACESSO-02:** O menu lateral e o painel inicial são determinados unicamente pelo perfil ativo.
- **RN-ACESSO-03:** O link "Mapa de Navegação" está disponível para todos os perfis.

## 6. Regras pendentes de validação
- Regras de validação de e-mail/senha no login (formato, tentativas, bloqueio) não especificadas.
- Fluxo completo de redefinição de senha (tela de nova senha, expiração do link) não especificado.
- Regra de atribuição de perfil(is) por usuário real (um usuário pode ter mais de um perfil simultâneo?) não especificada.
- Comportamento da busca global com filtragem de texto real não especificado.
- Estados de erro/carregamento do Login não especificados (diferente da tela de Pessoas, que modela esses estados explicitamente).
