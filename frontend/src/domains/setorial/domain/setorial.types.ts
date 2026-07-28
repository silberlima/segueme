export type StatusTransferencia = 'PENDENTE' | 'APROVADA' | 'RECUSADA'

export const STATUS_TRANSFERENCIA_LABEL: Record<StatusTransferencia, string> = {
  PENDENTE: 'Pendente',
  APROVADA: 'Aprovada',
  RECUSADA: 'Recusada',
}

export type StatusEmprestimo = 'SOLICITADO' | 'SUGESTOES_ENVIADAS' | 'EFETIVADO' | 'RECUSADO'

export const STATUS_EMPRESTIMO_LABEL: Record<StatusEmprestimo, string> = {
  SOLICITADO: 'Solicitado',
  SUGESTOES_ENVIADAS: 'Sugestões enviadas',
  EFETIVADO: 'Efetivado',
  RECUSADO: 'Recusado',
}

export type StatusCandidatoEmprestimo = 'SUGERIDO' | 'EFETIVADO'

/** Mudança definitiva de vínculo de uma pessoa entre paróquias (RF-040/041, RN-MOVIMENTO-01). */
export interface Transferencia {
  id: string
  pessoaId: string
  pessoaNome: string
  paroquiaOrigemId: string
  paroquiaOrigemNome: string
  paroquiaDestinoId: string
  paroquiaDestinoNome: string
  status: StatusTransferencia
  motivo: string | null
  justificativaRecusa: string | null
  createdAt: string
}

export interface SolicitarTransferenciaPayload {
  pessoaId: string
  paroquiaDestinoId: string
  motivo?: string
}

export interface EmprestimoCandidato {
  id: string
  pessoaId: string
  pessoaNome: string
  paroquiaOrigemId: string
  paroquiaOrigemNome: string
  status: StatusCandidatoEmprestimo
}

/** Uso temporário de um seguidor de outra paróquia numa equipe do Encontro corrente (RF-042 a 046, RN-18). */
export interface Emprestimo {
  id: string
  encontroId: string
  encontroNumero: number
  equipeId: string
  equipeNome: string
  paroquiaSolicitanteNome: string
  competenciaRequeridaId: string | null
  competenciaRequeridaNome: string | null
  status: StatusEmprestimo
  justificativaRecusa: string | null
  createdAt: string
  candidatos: EmprestimoCandidato[]
}

export interface SolicitarEmprestimoPayload {
  encontroId: string
  equipeId: string
  competenciaRequeridaId?: string
}

/** Candidato do pool de elegíveis a um Empréstimo (RF-043, RN-MOVIMENTO-05). */
export interface CandidatoElegivel {
  pessoaId: string
  pessoaNome: string
  paroquiaId: string
  paroquiaNome: string
  competencias: string[]
}

export type TipoSolicitacao = 'TRANSFERENCIA' | 'EMPRESTIMO'

/** Item do inbox único do Setorial (RF-046) — uma Transferência ou um Empréstimo pendente. */
export interface Solicitacao {
  id: string
  tipo: TipoSolicitacao
  createdAt: string
  descricao: string
  pessoaNome: string | null
  paroquiaOrigemNome: string | null
  paroquiaDestinoNome: string | null
  equipeNome: string | null
  encontroNumero: number | null
  competenciaNome: string | null
}
