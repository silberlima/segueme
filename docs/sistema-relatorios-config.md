# Sistema — Relatórios, Notificações e Configurações

## 1. Objetivo
Reunir as funcionalidades transversais de sistema: geração de relatórios, central de notificações e preferências de conta/configuração.

## 2. Telas envolvidas

### 2.1 Relatórios (`screen_relatorios`)
- **Objetivo:** gerar documentos consolidados a partir dos dados do sistema.
- **Componentes:** 6 cards de tipos de relatório (tipos exatos não detalhados no protótipo além do menu, que também lista "Relatórios" para múltiplos perfis — Nacional, Diocese, Setorial via dashboard, Finanças, Fichas); lista de "Gerados recentemente".
- **Ações:** gerar novo relatório (por tipo).

### 2.2 Notificações — Drawer (acessível de qualquer tela via sino no header)
- **Objetivo:** central de eventos do sistema relevantes ao usuário.
- **Tipos observados:** convite enviado, convite aceito, convite recusado (com trecho da justificativa).
- **Componentes:** ícone por tipo (info/ok/danger), título, texto, tempo relativo, indicador de não lida.
- **Ações:** marcar todas como lidas; abrir a notificação (navega ao contexto relacionado — inferido, não confirmado).
- Existe também um item de menu "Notificações" (`screen_notificacoes`) dentro do grupo Sistema, sugerindo uma versão em tela cheia do mesmo conteúdo do drawer — não aberta separadamente no protótipo revisado.

### 2.3 Configurações (`screen_config`)
- **Objetivo:** gerenciar preferências da conta do usuário.
- **Seções observadas:**
  - **Conta** — dados do usuário logado.
  - **Notificações** — toggles de ativação/desativação por tipo de notificação.
  - **Perfis de acesso** — gestão dos perfis de acesso disponíveis (relacionado à lista de 10 perfis do sistema).

## 3. Regras de negócio
- **RN-SISTEMA-01:** notificações são organizadas por tipo (informativa, positiva/ok, negativa/danger) e têm estado lida/não lida.
- **RN-SISTEMA-02:** o contador de notificações não lidas é exibido como badge no sino do header.

## 4. Casos de uso
- **UC — Consultar notificações:** Ator: qualquer usuário autenticado. Fluxo principal: clicar no sino → abrir drawer → visualizar lista.
- **UC — Gerar relatório:** Ator: perfis com item "Relatórios" no menu. Fluxo principal: abrir Relatórios → escolher tipo → gerar. Pós-condição: relatório listado em "Gerados recentemente".

## 5. Regras pendentes de validação
- Tipos exatos dos 6 relatórios disponíveis não foram detalhados no protótipo (apenas a existência de 6 cards) — precisa ser especificado pelo Product Owner.
- Formato de saída dos relatórios (PDF, planilha, impressão) não especificado.
- Conteúdo e opções detalhadas da seção "Perfis de acesso" em Configurações (é ali que se atribuiriam perfis a usuários reais?) não foi aberto/detalhado no protótipo — ver também pendência de atribuição de perfil em `autenticacao-acesso.md`.
- Regras de quem pode alterar configurações de Notificações de outros usuários (individual vs. administração global) não especificadas.
