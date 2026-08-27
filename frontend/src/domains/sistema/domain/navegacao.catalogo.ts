import type { ItemNavegacao } from './navegacao.types'
import type { Perfil } from './perfil.types'

/**
 * Os nove perfis operacionais — todos menos o Seguidor. Todo usuário do sistema também é um
 * seguidor, mas o perfil Seguidor não é um posto de trabalho: quem está nele enxerga apenas os
 * próprios dados (ver `PERFIS_SEGUIDOR` abaixo).
 */
const OPERACIONAIS: Perfil[] = [
  'NACIONAL',
  'DIOCESE',
  'SETORIAL',
  'PAROQUIA',
  'EQUIPE_FICHAS',
  'EQUIPE_MONTAGEM',
  'EQUIPE_COMANDO',
  'EQUIPE_FINANCAS',
  'POS_ENCONTRO',
]

/**
 * Quem cuida do cadastro de pessoas. O Nacional fica de fora por decisão do PO: no nível nacional
 * se cuida de dioceses, setores e paróquias — gente é assunto de quem convive com ela.
 */
const CUIDAM_DE_PESSOAS: Perfil[] = OPERACIONAIS.filter((perfil) => perfil !== 'NACIONAL')

/**
 * Catálogo de telas do sistema (autenticacao-acesso.md 2.3 — Mapa de Navegação).
 * Cada fase do plano de implementação (`plano-implementacao.md`) atualiza uma entrada
 * de 'previsto' para 'pronto' e preenche `path` ao entregar a tela correspondente.
 */
export const CATALOGO_NAVEGACAO: ItemNavegacao[] = [
  { titulo: 'Mapa de Navegação', modulo: 'Acesso', status: 'pronto', path: '/mapa', perfis: 'todos' },
  { titulo: 'Notificações', modulo: 'Sistema', status: 'pronto', path: '/notificacoes', perfis: 'todos' },
  {
    titulo: 'Relatórios',
    modulo: 'Sistema',
    status: 'pronto',
    path: '/relatorios',
    // Todo perfil operacional gera algum relatório; quais tipos cada um vê é decidido pelo backend
    // (`TipoRelatorio`). SEGUIDOR não gera nenhum, então não vê o item — ver
    // `sistema-relatorios-config.md` 6.1.
    perfis: [
      'NACIONAL',
      'DIOCESE',
      'SETORIAL',
      'PAROQUIA',
      'EQUIPE_FICHAS',
      'EQUIPE_MONTAGEM',
      'EQUIPE_COMANDO',
      'EQUIPE_FINANCAS',
      'POS_ENCONTRO',
    ],
  },
  { titulo: 'Configurações', modulo: 'Sistema', status: 'pronto', path: '/configuracoes', perfis: 'todos' },

  // Um painel por perfil operacional (RN-DASH-01): cada item é visível só ao seu perfil, e é o
  // destino da entrada no sistema (ver `PanelRedirect`).
  { titulo: 'Painel Nacional', modulo: 'Dashboards', status: 'pronto', path: '/painel/nacional', perfis: ['NACIONAL'] },
  { titulo: 'Painel da Diocese', modulo: 'Dashboards', status: 'pronto', path: '/painel/diocese', perfis: ['DIOCESE'] },
  { titulo: 'Painel do Setor', modulo: 'Dashboards', status: 'pronto', path: '/painel/setor', perfis: ['SETORIAL'] },
  { titulo: 'Painel da Paróquia', modulo: 'Dashboards', status: 'pronto', path: '/painel/paroquia', perfis: ['PAROQUIA'] },
  { titulo: 'Painel de Fichas', modulo: 'Dashboards', status: 'pronto', path: '/painel/fichas', perfis: ['EQUIPE_FICHAS'] },
  {
    titulo: 'Painel de Montagem',
    modulo: 'Dashboards',
    status: 'pronto',
    path: '/painel/montagem',
    perfis: ['EQUIPE_MONTAGEM'],
  },
  {
    titulo: 'Painel de Comando',
    modulo: 'Dashboards',
    status: 'pronto',
    path: '/painel/comando',
    perfis: ['EQUIPE_COMANDO'],
  },
  {
    titulo: 'Painel Financeiro',
    modulo: 'Dashboards',
    status: 'pronto',
    path: '/painel/financas',
    perfis: ['EQUIPE_FINANCAS'],
  },
  {
    titulo: 'Painel Pós-Encontro',
    modulo: 'Dashboards',
    status: 'pronto',
    path: '/painel/pos-encontro',
    perfis: ['POS_ENCONTRO'],
  },
  // As duas telas do Seguidor. Visíveis só a ele: quem está num perfil operacional chega aos
  // próprios dados pelo mesmo caminho, trocando o perfil ativo para Seguidor.
  { titulo: 'Meus Dados', modulo: 'Dashboards', status: 'pronto', path: '/meus-dados', perfis: ['SEGUIDOR'] },
  {
    titulo: 'Meu Histórico',
    modulo: 'Dashboards',
    status: 'pronto',
    path: '/meus-dados/historico',
    perfis: ['SEGUIDOR'],
  },

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

  { titulo: 'Lista de Pessoas', modulo: 'Pessoas', status: 'pronto', path: '/pessoas', perfis: CUIDAM_DE_PESSOAS },
  { titulo: 'Cadastrar Pessoa', modulo: 'Pessoas', status: 'pronto', path: '/pessoas/novo', perfis: CUIDAM_DE_PESSOAS },
  { titulo: 'Perfil da Pessoa', modulo: 'Pessoas', status: 'pronto', path: null, perfis: CUIDAM_DE_PESSOAS },
  { titulo: 'Histórico da Pessoa', modulo: 'Pessoas', status: 'pronto', path: null, perfis: CUIDAM_DE_PESSOAS },
  { titulo: 'Casais', modulo: 'Pessoas', status: 'pronto', path: '/pessoas/casais', perfis: CUIDAM_DE_PESSOAS },
  { titulo: 'Competências', modulo: 'Pessoas', status: 'pronto', path: '/pessoas/competencias', perfis: CUIDAM_DE_PESSOAS },

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

  { titulo: 'Equipes do Encontro', modulo: 'Equipes & Convites', status: 'pronto', path: null, perfis: OPERACIONAIS },
  {
    titulo: 'Sugestões Inteligentes',
    modulo: 'Equipes & Convites',
    status: 'pronto',
    path: null,
    perfis: ['EQUIPE_MONTAGEM'],
  },
  { titulo: 'Substituições', modulo: 'Equipes & Convites', status: 'pronto', path: null, perfis: ['EQUIPE_MONTAGEM'] },
  { titulo: 'Convites', modulo: 'Equipes & Convites', status: 'pronto', path: null, perfis: OPERACIONAIS },
  { titulo: 'Aceites', modulo: 'Equipes & Convites', status: 'pronto', path: null, perfis: OPERACIONAIS },
  { titulo: 'Recusas', modulo: 'Equipes & Convites', status: 'pronto', path: null, perfis: OPERACIONAIS },
  { titulo: 'Painel do Coordenador', modulo: 'Equipes & Convites', status: 'pronto', path: null, perfis: OPERACIONAIS },
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
  { titulo: 'Lista de Espera', modulo: 'Inscrições', status: 'pronto', path: null, perfis: OPERACIONAIS },
  { titulo: 'Participantes', modulo: 'Inscrições', status: 'pronto', path: null, perfis: OPERACIONAIS },

  { titulo: 'Palestras', modulo: 'Conteúdo', status: 'pronto', path: null, perfis: OPERACIONAIS },
  { titulo: 'Palestrantes', modulo: 'Conteúdo', status: 'pronto', path: null, perfis: OPERACIONAIS },

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
