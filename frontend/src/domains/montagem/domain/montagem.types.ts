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
