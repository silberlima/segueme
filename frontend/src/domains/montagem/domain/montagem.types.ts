export type PapelIntegrante = 'COORDENADOR' | 'INTEGRANTE'

export const PAPEL_INTEGRANTE_LABEL: Record<PapelIntegrante, string> = {
  COORDENADOR: 'Coordenador',
  INTEGRANTE: 'Integrante',
}

export type StatusIntegrante = 'PENDENTE' | 'ACEITO' | 'RECUSADO'

export const STATUS_INTEGRANTE_LABEL: Record<StatusIntegrante, string> = {
  PENDENTE: 'Pendente',
  ACEITO: 'Aceito',
  RECUSADO: 'Recusado',
}

export interface Integrante {
  id: string
  pessoaId: string
  pessoaNome: string
  papel: PapelIntegrante
  status: StatusIntegrante
}

export interface EquipeResumo {
  id: string
  nome: string
  minimo: number
  maximo: number
  preenchidos: number
  vagasAbertas: number
  completa: boolean
}

export interface EquipeDetalhe extends EquipeResumo {
  preRequisitoMinEncontros: number | null
  competenciasRequeridas: string[]
  integrantes: Integrante[]
}

export interface ScoreBreakdown {
  historicoEquipesSemelhantes: number
  competencias: number
  frequenciaPosEncontro: number
  preRequisito: number
}

/**
 * Candidato ranqueado pelo `SuggestionEngine` do backend (RF-030) — o score já vem calculado
 * pela API (pesos 35/30/20/15%, RN-09/RN-MONTAGEM-03); o frontend só exibe, nunca recalcula.
 */
export interface CandidatoSugerido {
  pessoaId: string
  pessoaNome: string
  idade: number
  situacao: string
  paroquiaNome: string
  mesmaParoquia: boolean
  requerEmprestimoSetor: boolean
  scoreTotal: number
  scoreVersao: string
  breakdown: ScoreBreakdown
  justificativas: string[]
}

export interface Substituicao {
  id: string
  equipeNome: string
  pessoaSaiId: string
  pessoaSaiNome: string
  motivoSaida: string
  pessoaEntraId: string
  pessoaEntraNome: string
  justificativa: string
  dataSubstituicao: string
}

export interface NovaSubstituicaoPayload {
  encontroId: string
  equipeId: string
  pessoaSaiId: string
  motivoSaida: string
  pessoaEntraId: string
  justificativa: string
}

/**
 * Um convite (RF-034 a RF-039) — no backend é o próprio `IntegranteEquipe` (Fase 7 reaproveita a
 * entidade da Fase 6), aqui exposto com os campos do ciclo de convite/aceite/recusa.
 */
export interface Convite {
  id: string
  encontroId: string
  equipeId: string
  equipeNome: string
  pessoaId: string
  pessoaNome: string
  papel: PapelIntegrante
  status: StatusIntegrante
  dataEnvioConvite: string
  dataResposta: string | null
  justificativaRecusa: string | null
  reenvios: number
}

export interface EmitirConvitePayload {
  encontroId: string
  equipeId: string
  pessoaId: string
  papel: PapelIntegrante
}
