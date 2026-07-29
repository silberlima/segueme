import type { ItemNavegacao } from './navegacao.types'

/**
 * Catálogo de telas do sistema (autenticacao-acesso.md 2.3 — Mapa de Navegação).
 * Cada fase do plano de implementação (`plano-implementacao.md`) atualiza uma entrada
 * de 'previsto' para 'pronto' e preenche `path` ao entregar a tela correspondente.
 */
export const CATALOGO_NAVEGACAO: ItemNavegacao[] = [
  { titulo: 'Mapa de Navegação', modulo: 'Acesso', status: 'pronto', path: '/mapa', perfis: 'todos' },
  { titulo: 'Notificações', modulo: 'Sistema', status: 'previsto', path: null, perfis: 'todos' },
  { titulo: 'Relatórios', modulo: 'Sistema', status: 'previsto', path: null, perfis: 'todos' },
  { titulo: 'Configurações', modulo: 'Sistema', status: 'previsto', path: null, perfis: 'todos' },

  { titulo: 'Painel Nacional', modulo: 'Dashboards', status: 'previsto', path: null, perfis: ['NACIONAL'] },
  { titulo: 'Painel da Diocese', modulo: 'Dashboards', status: 'previsto', path: null, perfis: ['DIOCESE'] },
  { titulo: 'Painel do Setor', modulo: 'Dashboards', status: 'previsto', path: null, perfis: ['SETORIAL'] },
  { titulo: 'Painel da Paróquia', modulo: 'Dashboards', status: 'previsto', path: null, perfis: ['PAROQUIA'] },
  { titulo: 'Painel de Fichas', modulo: 'Dashboards', status: 'previsto', path: null, perfis: ['EQUIPE_FICHAS'] },
  { titulo: 'Painel de Montagem', modulo: 'Dashboards', status: 'previsto', path: null, perfis: ['EQUIPE_MONTAGEM'] },
  { titulo: 'Painel de Comando', modulo: 'Dashboards', status: 'previsto', path: null, perfis: ['EQUIPE_COMANDO'] },
  { titulo: 'Painel Financeiro', modulo: 'Dashboards', status: 'previsto', path: null, perfis: ['EQUIPE_FINANCAS'] },
  { titulo: 'Painel Pós-Encontro', modulo: 'Dashboards', status: 'previsto', path: null, perfis: ['POS_ENCONTRO'] },
  { titulo: 'Meu Perfil', modulo: 'Dashboards', status: 'previsto', path: null, perfis: ['SEGUIDOR'] },

  {
    titulo: 'Dioceses e Setores',
    modulo: 'Território',
    status: 'pronto',
    path: '/hierarquia/dioceses',
    perfis: ['NACIONAL', 'DIOCESE'],
  },
  {
    titulo: 'Paróquias',
    modulo: 'Território',
    status: 'pronto',
    path: '/hierarquia/paroquias',
    perfis: ['NACIONAL', 'DIOCESE', 'SETORIAL'],
  },

  { titulo: 'Lista de Pessoas', modulo: 'Pessoas', status: 'pronto', path: '/pessoas', perfis: 'todos' },
  { titulo: 'Cadastrar Pessoa', modulo: 'Pessoas', status: 'pronto', path: '/pessoas/novo', perfis: 'todos' },
  { titulo: 'Perfil da Pessoa', modulo: 'Pessoas', status: 'pronto', path: null, perfis: 'todos' },
  { titulo: 'Histórico da Pessoa', modulo: 'Pessoas', status: 'pronto', path: null, perfis: 'todos' },
  { titulo: 'Casais', modulo: 'Pessoas', status: 'pronto', path: '/pessoas/casais', perfis: 'todos' },
  { titulo: 'Competências', modulo: 'Pessoas', status: 'pronto', path: '/pessoas/competencias', perfis: 'todos' },

  {
    titulo: 'Lista de Encontros',
    modulo: 'Encontros',
    status: 'pronto',
    path: '/encontros',
    perfis: ['NACIONAL', 'DIOCESE', 'SETORIAL', 'PAROQUIA'],
  },
  { titulo: 'Criar Encontro', modulo: 'Encontros', status: 'pronto', path: '/encontros/novo', perfis: ['PAROQUIA'] },
  {
    titulo: 'Detalhe do Encontro',
    modulo: 'Encontros',
    status: 'pronto',
    path: null,
    perfis: ['NACIONAL', 'DIOCESE', 'SETORIAL', 'PAROQUIA'],
  },
  {
    titulo: 'Equipe Dirigente',
    modulo: 'Encontros',
    status: 'pronto',
    path: null,
    perfis: ['NACIONAL', 'DIOCESE', 'SETORIAL', 'PAROQUIA'],
  },
  {
    titulo: 'Equipe Comando',
    modulo: 'Encontros',
    status: 'pronto',
    path: null,
    perfis: ['NACIONAL', 'DIOCESE', 'SETORIAL', 'PAROQUIA'],
  },

  { titulo: 'Equipes do Encontro', modulo: 'Equipes & Convites', status: 'pronto', path: null, perfis: 'todos' },
  {
    titulo: 'Sugestões Inteligentes',
    modulo: 'Equipes & Convites',
    status: 'pronto',
    path: null,
    perfis: ['EQUIPE_MONTAGEM'],
  },
  { titulo: 'Substituições', modulo: 'Equipes & Convites', status: 'pronto', path: null, perfis: ['EQUIPE_MONTAGEM'] },
  { titulo: 'Convites', modulo: 'Equipes & Convites', status: 'pronto', path: null, perfis: 'todos' },
  { titulo: 'Aceites', modulo: 'Equipes & Convites', status: 'pronto', path: null, perfis: 'todos' },
  { titulo: 'Recusas', modulo: 'Equipes & Convites', status: 'pronto', path: null, perfis: 'todos' },
  { titulo: 'Painel do Coordenador', modulo: 'Equipes & Convites', status: 'pronto', path: null, perfis: 'todos' },
  {
    titulo: 'Transferências',
    modulo: 'Equipes & Convites',
    status: 'pronto',
    path: '/setorial/transferencias',
    // Corrigido do seed original ['SETORIAL','EQUIPE_MONTAGEM'] (cópia do item Empréstimos):
    // `transferencias-e-emprestimos.md` 2.1 diz "perfis de Pessoas/Paróquia (solicitação) e
    // Setorial (decisão)" — Montagem não solicita Transferência, só Empréstimo.
    perfis: ['SETORIAL', 'NACIONAL', 'DIOCESE', 'PAROQUIA', 'EQUIPE_FICHAS'],
  },
  {
    titulo: 'Empréstimos',
    modulo: 'Equipes & Convites',
    status: 'pronto',
    path: '/setorial/emprestimos',
    perfis: ['SETORIAL', 'EQUIPE_MONTAGEM'],
  },
  {
    titulo: 'Solicitações (inbox)',
    modulo: 'Equipes & Convites',
    status: 'pronto',
    path: '/setorial/solicitacoes',
    perfis: ['SETORIAL'],
  },

  { titulo: 'Pré-inscrições', modulo: 'Inscrições', status: 'pronto', path: null, perfis: ['EQUIPE_FICHAS'] },
  { titulo: 'Ficha de Inscrição', modulo: 'Inscrições', status: 'pronto', path: null, perfis: ['EQUIPE_FICHAS'] },
  { titulo: 'Lista de Espera', modulo: 'Inscrições', status: 'pronto', path: null, perfis: 'todos' },
  { titulo: 'Participantes', modulo: 'Inscrições', status: 'pronto', path: null, perfis: 'todos' },

  { titulo: 'Palestras', modulo: 'Conteúdo', status: 'pronto', path: null, perfis: 'todos' },
  { titulo: 'Palestrantes', modulo: 'Conteúdo', status: 'pronto', path: null, perfis: 'todos' },

  {
    titulo: 'Visão Geral Financeira',
    modulo: 'Finanças',
    status: 'pronto',
    path: null,
    perfis: ['EQUIPE_FINANCAS', 'PAROQUIA'],
  },
  { titulo: 'Receitas', modulo: 'Finanças', status: 'pronto', path: null, perfis: ['EQUIPE_FINANCAS'] },
  { titulo: 'Despesas', modulo: 'Finanças', status: 'pronto', path: null, perfis: ['EQUIPE_FINANCAS'] },
  { titulo: 'Campanhas', modulo: 'Finanças', status: 'pronto', path: null, perfis: ['EQUIPE_FINANCAS'] },

  {
    titulo: 'Presenças',
    modulo: 'Pós-Encontro',
    status: 'pronto',
    path: '/pos-encontro/presencas',
    perfis: ['POS_ENCONTRO', 'PAROQUIA'],
  },
  {
    titulo: 'Frequência',
    modulo: 'Pós-Encontro',
    status: 'pronto',
    path: '/pos-encontro/frequencia',
    perfis: ['POS_ENCONTRO', 'PAROQUIA'],
  },
  {
    titulo: 'Prioridades',
    modulo: 'Pós-Encontro',
    status: 'pronto',
    path: '/pos-encontro/prioridades',
    perfis: ['POS_ENCONTRO', 'PAROQUIA', 'EQUIPE_MONTAGEM'],
  },
]
