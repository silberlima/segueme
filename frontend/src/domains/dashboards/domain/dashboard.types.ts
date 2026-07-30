/**
 * Contratos dos nove painéis por perfil (RF-053, `dashboards-por-perfil.md`).
 *
 * Os tipos são declarados aqui, e não importados dos domínios de origem (montagem, financeiro,
 * inscrições…), porque `domains/*` não importa de dentro de outro domínio
 * (`skills/padrao-componentes-react-tailwind.md` 3): o painel consome o contrato do endpoint de
 * dashboard, que é próprio, mesmo quando o formato coincide com o da tela de detalhe.
 */

export type RegiaoBrasil = 'NORDESTE' | 'SUDESTE' | 'SUL' | 'CENTRO_OESTE' | 'NORTE'

export const REGIAO_LABEL: Record<RegiaoBrasil, string> = {
  NORDESTE: 'Nordeste',
  SUDESTE: 'Sudeste',
  SUL: 'Sul',
  CENTRO_OESTE: 'Centro-Oeste',
  NORTE: 'Norte',
}

export type TipoPendencia = 'EQUIPES_INCOMPLETAS' | 'CADASTRO_RESERVA' | 'FICHAS_A_PREENCHER'

export const PENDENCIA_LABEL: Record<TipoPendencia, string> = {
  EQUIPES_INCOMPLETAS: 'Equipes incompletas',
  CADASTRO_RESERVA: 'Candidatos no cadastro de reserva',
  FICHAS_A_PREENCHER: 'Fichas de inscrição a preencher',
}

export interface Pendencia {
  tipo: TipoPendencia
  quantidade: number
}

/** `regiao` nula = dioceses sem região informada (a hierarquia não tem tela de cadastro). */
export interface ContagemRegiao {
  regiao: RegiaoBrasil | null
  total: number
}

export interface ContagemTerritorio {
  territorioId: string
  nome: string
  total: number
}

/** `mes` no formato `yyyy-MM`. */
export interface ContagemMensal {
  mes: string
  total: number
}

export interface DioceseDestaque {
  dioceseId: string
  nome: string
  paroquias: number
  seguidores: number
  encontrosNoAno: number
}

export interface ParoquiaDoSetor {
  paroquiaId: string
  nome: string
  seguidores: number
  proximoEncontro: string | null
}

/** O Encontro como os painéis o exibem (cartão do corrente e tabela de recentes). */
export interface EncontroDoPainel {
  id: string
  numero: number
  padroeiro: string
  status: 'ATIVO' | 'ENCERRADO'
  dataInicio: string
  dataTermino: string
  local: string
  vagas: number
  paroquiaId: string
  paroquiaNome: string
  diretorEspiritualNome: string
}

export interface EquipeResumoDoPainel {
  id: string
  nome: string
  minimo: number
  maximo: number
  preenchidos: number
  vagasAbertas: number
  completa: boolean
}

export interface ScoreBreakdownDoPainel {
  historicoEquipesSemelhantes: number
  competencias: number
  frequenciaPosEncontro: number
  preRequisito: number
}

export interface CandidatoSugeridoDoPainel {
  pessoaId: string
  pessoaNome: string
  idade: number
  situacao: string
  paroquiaNome: string
  mesmaParoquia: boolean
  requerEmprestimoSetor: boolean
  scoreTotal: number
  scoreVersao: string
  breakdown: ScoreBreakdownDoPainel
  justificativas: string[]
}

export interface ConviteDoPainel {
  id: string
  encontroId: string
  equipeId: string
  equipeNome: string
  pessoaId: string
  pessoaNome: string
  papel: 'COORDENADOR' | 'INTEGRANTE'
  status: 'PENDENTE' | 'ACEITO' | 'RECUSADO'
  dataEnvioConvite: string
  dataResposta: string | null
  justificativaRecusa: string | null
  reenvios: number
}

export interface AceitesPorEquipe {
  equipeId: string
  equipeNome: string
  aceitos: number
  convidados: number
}

export interface SolicitacaoDoPainel {
  id: string
  tipo: 'TRANSFERENCIA' | 'EMPRESTIMO'
  createdAt: string
  descricao: string
  pessoaNome: string | null
  paroquiaOrigemNome: string | null
  paroquiaDestinoNome: string | null
  equipeNome: string | null
  encontroNumero: number | null
  competenciaNome: string | null
}

export interface PreInscricaoDoPainel {
  id: string
  pessoaId: string
  pessoaNome: string
  idade: number
  indicadoPorNome: string | null
  encontroId: string
  status: 'AGUARDANDO_FICHA' | 'EM_ANALISE' | 'CONFIRMADA'
  recebidaEm: string
}

export interface ReservaDoPainel {
  id: string
  ordem: number
  pessoaId: string
  pessoaNome: string
  paroquiaNome: string
  naFilaDesde: string
  status: 'AGUARDANDO' | 'PROMOVIDO'
}

export interface ResumoFinanceiroDoPainel {
  saldo: number
  totalReceitas: number
  totalDespesas: number
  campanhasAtivas: number
}

export interface CampanhaDoPainel {
  id: string
  encontroId: string
  encontroNumero: number
  nome: string
  meta: number
  valorArrecadado: number
  status: 'ATIVA' | 'ENCERRADA'
  createdAt: string
}

export interface LancamentoDoPainel {
  id: string
  encontroId: string
  encontroNumero: number
  tipo: 'RECEITA' | 'DESPESA'
  categoria: string
  valor: number
  data: string
  situacao: 'PENDENTE' | 'APROVADA' | 'PAGA' | null
  aprovadoPor: string | null
  createdAt: string
}

export interface EncontroPosDoPainel {
  id: string
  paroquiaId: string
  paroquiaNome: string
  data: string
  tema: string | null
  createdAt: string
}

export interface FrequenciaDoPainel {
  pessoaId: string
  pessoaNome: string
  totalEncontros: number
  presencas: number
  percentual: number
  faixaPrioridade: 'ALTA' | 'MEDIA' | 'BAIXA'
}

export interface DashboardNacional {
  seguidores: number
  dioceses: number
  paroquias: number
  encontrosNoAno: number
  encontrosPorRegiao: ContagemRegiao[]
  diocesesDestaque: DioceseDestaque[]
}

export interface DashboardDiocese {
  seguidores: number
  setores: number
  paroquias: number
  encontrosNoAno: number
  seguidoresPorSetor: ContagemTerritorio[]
  solicitacoesAoSetorial: number
}

export interface DashboardSetorial {
  solicitacoesPendentes: number
  transferenciasNoMes: number
  emprestimosAtivos: number
  seguidores: number
  solicitacoes: SolicitacaoDoPainel[]
  paroquias: ParoquiaDoSetor[]
}

export interface DashboardParoquia {
  seguidoresAtivos: number
  encontrosRealizados: number
  diasParaProximoEncontro: number | null
  totalPendencias: number
  encontroCorrente: EncontroDoPainel | null
  inscricoesConfirmadas: number
  equipes: EquipeResumoDoPainel[]
  encontrosRecentes: EncontroDoPainel[]
  pendencias: Pendencia[]
  financeiro: ResumoFinanceiroDoPainel
  campanhas: CampanhaDoPainel[]
}

export interface DashboardFichas {
  aguardandoFicha: number
  emAnalise: number
  confirmados: number
  vagasRestantes: number
  cadastroReserva: number
  seguimistas: number
  seguidores: number
  encontroCorrente: EncontroDoPainel | null
  preInscricoesAguardandoFicha: PreInscricaoDoPainel[]
  reserva: ReservaDoPainel[]
}

export interface DashboardMontagem {
  equipesIncompletas: number
  vagasAbertas: number
  sugestoesDisponiveis: number
  recusas: number
  encontroCorrente: EncontroDoPainel | null
  equipes: EquipeResumoDoPainel[]
  equipePrioritaria: EquipeResumoDoPainel | null
  sugestoes: CandidatoSugeridoDoPainel[]
}

export interface DashboardComando {
  convitesEnviados: number
  aceites: number
  pendentes: number
  recusas: number
  encontroCorrente: EncontroDoPainel | null
  convitesRecentes: ConviteDoPainel[]
  aceitesPorEquipe: AceitesPorEquipe[]
}

export interface DashboardFinancas {
  resumo: ResumoFinanceiroDoPainel
  encontroCorrente: EncontroDoPainel | null
  campanhas: CampanhaDoPainel[]
  lancamentosRecentes: LancamentoDoPainel[]
}

export interface DashboardPosEncontro {
  jovensAcompanhados: number
  presencaMedia: number
  encontrosPosNoMes: number
  prioridadeAlta: number
  frequenciaMensal: ContagemMensal[]
  proximoEncontroPos: EncontroPosDoPainel | null
  chamadaRegistrada: number
  prioridades: FrequenciaDoPainel[]
}
