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
- Tipos exatos dos 6 relatórios disponíveis não foram detalhados no protótipo (apenas a existência de 6 cards) — precisa ser especificado pelo Product Owner. *(Fase 13: seis tipos adotados por melhor esforço — ver seção 6.1.)*
- Formato de saída dos relatórios (PDF, planilha, impressão) não especificado. *(Fase 13: CSV no MVP — ver 6.3.)*
- Conteúdo e opções detalhadas da seção "Perfis de acesso" em Configurações (é ali que se atribuiriam perfis a usuários reais?) não foi aberto/detalhado no protótipo — ver também pendência de atribuição de perfil em `autenticacao-acesso.md`. *(Fase 13: seção somente de leitura — ver 6.5.)*
- Regras de quem pode alterar configurações de Notificações de outros usuários (individual vs. administração global) não especificadas. *(Fase 13: apenas as próprias — ver 6.4.)*

---

## 6. Decisões da implementação (Fase 13) — sujeitas a validação do PO

Registro do que a Fase 13 adotou onde o protótipo não determina a regra. Tudo aqui é melhor esforço documentado, não decisão fechada.

### 6.1 Os seis tipos de relatório (pendência #15-sistema)
O protótipo mostra seis cartões sem dizer quais são. Adotamos **um tipo por módulo de dados já implementado**, de forma que todo relatório saia de dado real em vez de placeholder:

| Tipo | Conteúdo | Perfis que podem gerar |
|---|---|---|
| Pessoas | Cadastro do escopo territorial, com paróquia, situação e competências | Nacional, Diocese, Setorial, Paróquia, Equipe Fichas |
| Encontros | Encontros do escopo, com paróquia, datas, vagas e situação | Nacional, Diocese, Setorial, Paróquia |
| Inscrições | Funil do Encontro corrente, etapa a etapa | Paróquia, Equipe Fichas |
| Equipes e convites | Composição das equipes do Encontro corrente e o estado de cada convite | Paróquia, Equipe Montagem, Equipe Comando |
| Financeiro | Lançamentos de receita e despesa do Encontro corrente | Paróquia, Equipe Finanças |
| Pós-encontro | Frequência acumulada dos acompanhados e a faixa de prioridade | Paróquia, Pós-Encontro |

A lista vive num único enum (`TipoRelatorio`), com os perfis autorizados ao lado de cada tipo: trocá-la quando o PO a definir é mexer em um lugar só. **Seguidor não gera relatório algum** e por isso não vê o item "Relatórios" no menu.

Os cartões exibidos vêm do backend (`GET /api/v1/relatorios/tipos`), recortados pelas **roles do token** — não pelo perfil ativo na tela. É o mesmo critério de toda autorização do sistema (os painéis da Fase 12 seguem-no): o perfil ativo é uma escolha de navegação do frontend e não pode servir de base para autorizar. Um usuário com dois perfis vê a união dos tipos dos dois.

### 6.2 Escopo do relatório
Nenhum endpoint recebe território por parâmetro: o escopo vem das claims do solicitante (RN-04), como nos painéis. Ele é **gravado junto do pedido** porque a geração roda depois do commit, num thread sem contexto de segurança — não haveria como resolvê-lo de novo na hora de gerar.

Inscrições, Equipes e Financeiro são sobre o **Encontro corrente** da paróquia (mesmo critério da Fase 12, `dashboards-por-perfil.md` 6.2), já que a tela Relatórios não tem seletor de Encontro no protótipo. Paróquia sem nenhum Encontro produz um relatório com situação "Falhou" e o motivo explícito, em vez de um arquivo vazio que pareceria correto.

### 6.3 Geração assíncrona e formato de saída (pendência #37)
A geração **não acontece dentro da request** (`skills/10`): o `POST` responde `202 Accepted` com o relatório `PENDENTE`, um evento pós-commit dispara a geração num pool dedicado e a tela acompanha até `CONCLUIDO` ou `FALHOU`. A conclusão também chega ao solicitante como notificação (RN-SISTEMA-01) — é assim que ele sabe que o arquivo ficou pronto sem ficar recarregando a tela.

O formato de saída não está decidido. O MVP entrega **CSV** (separador `;`, aspas RFC 4180), que qualquer planilha abre. O gerador devolve colunas e linhas, não texto: trocar para PDF ou XLSX depois é escrever outro serializador, não refazer os seis geradores.

O arquivo é guardado na própria linha do relatório porque o projeto ainda não tem armazenamento de arquivos; quando o formato for decidido, é esse campo que vira referência para o arquivo externo.

"Gerados recentemente" lista os relatórios **do próprio solicitante**, e só ele baixa o que pediu: um relatório carrega o escopo de quem o gerou, então exibi-lo a outro usuário mostraria dado apurado sob um escopo que não é o dele.

### 6.4 Preferências de notificação (pendência #34)
Um toggle por tipo (informativa, positiva, negativa), gravado só quando o usuário salva. **A tabela guarda apenas a escolha explícita**: ausência de registro significa "recebe", então quem nunca abriu Configurações continua recebendo tudo, sem seed por usuário.

Quem poderia alterar as preferências de outro usuário não foi decidido, então **não existe esse caminho**: o `usuarioId` vem sempre do `sub` do token e não há administração global. Abrir o endpoint criaria um poder que ninguém validou.

A preferência é respeitada num ponto único (`PublicarNotificacaoUseCase`), por onde toda notificação passa — nenhum domínio que notifica precisa conhecer a regra.

### 6.5 Configurações: Conta e Perfis de acesso
As duas seções são **somente leitura**. Os perfis vêm hoje das roles do Keycloak, e quem atribui perfil a um usuário real é a pendência #3 — permitir editar aqui daria a impressão de um poder que o sistema não tem. A seção lista os perfis concedidos e marca o ativo; a troca continua sendo no menu lateral (RN-ACESSO-02).

### 6.6 Contador do sino
O badge (RN-SISTEMA-02) passou a vir de uma contagem própria (`GET /api/v1/notificacoes/nao-lidas`) em vez do tamanho da primeira página do drawer, que estaria errado assim que houvesse mais notificações do que cabem na página. Como notificação nasce de evento do servidor — uma recusa de convite, um relatório que ficou pronto — e não de uma ação do usuário que o frontend pudesse invalidar, o contador se reconsulta periodicamente e a lista é sempre recarregada ao abrir.
