export type StatusPreInscricao = 'AGUARDANDO_FICHA' | 'EM_ANALISE' | 'CONFIRMADA'

export const STATUS_PRE_INSCRICAO_LABEL: Record<StatusPreInscricao, string> = {
  AGUARDANDO_FICHA: 'Aguardando ficha',
  EM_ANALISE: 'Em análise',
  CONFIRMADA: 'Confirmada',
}

export type StatusPagamento = 'PENDENTE' | 'PAGO'

export const STATUS_PAGAMENTO_LABEL: Record<StatusPagamento, string> = {
  PENDENTE: 'Pendente',
  PAGO: 'Pago',
}

export type StatusReserva = 'AGUARDANDO' | 'PROMOVIDO'

export interface PreInscricao {
  id: string
  pessoaId: string
  pessoaNome: string
  idade: number
  indicadoPorNome: string | null
  encontroId: string
  status: StatusPreInscricao
  recebidaEm: string
}

export interface FichaInscricao {
  id: string
  preInscricaoId: string
  pessoaId: string
  pessoaNome: string
  encontroId: string
  statusPagamento: StatusPagamento
  createdAt: string
}

export interface CadastroReserva {
  id: string
  ordem: number
  pessoaId: string
  pessoaNome: string
  paroquiaNome: string
  naFilaDesde: string
  status: StatusReserva
}

export interface Funil {
  preInscritos: number
  emAnalise: number
  confirmados: number
  emReserva: number
  seguidores: number
}

export interface Participante {
  fichaInscricaoId: string
  pessoaId: string
  pessoaNome: string
  paroquiaNome: string
  statusPagamento: StatusPagamento
}

export interface NovaPreInscricaoPayload {
  pessoaId: string
  encontroId: string
}

/** Exatamente um dos dois campos vem preenchido — cair na reserva não é um erro (RN-INSCRICAO-03). */
export interface CriarPreInscricaoResultado {
  emReserva: boolean
  preInscricao: PreInscricao | null
  cadastroReserva: CadastroReserva | null
}

export interface EncontroContexto {
  id: string
  numero: number
  padroeiro: string
  vagas: number
  paroquiaId: string
}

/**
 * Subconjunto de `PessoaResponse` (`domains/pessoas`) usado pela Ficha de Inscrição. As 4 seções
 * da Ficha já vivem no cadastro nacional da Pessoa (Fase 3) — não são reintroduzidas aqui.
 */
export interface PessoaCompleta {
  id: string
  nomeCompleto: string
  dataNascimento: string
  telefone: string
  estadoCivil: string
  paroquiaNome: string
  alergias: string | null
  restricaoAlimentar: string | null
  medicacaoUsoContinuo: string | null
  contatoEmergenciaNome: string | null
  contatoEmergenciaTelefone: string | null
  contatoEmergenciaParentesco: string | null
  competencias: string[]
  indicadoPorNome: string | null
}
