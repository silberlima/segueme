export type FaixaPrioridade = 'ALTA' | 'MEDIA' | 'BAIXA'

export const FAIXA_PRIORIDADE_LABEL: Record<FaixaPrioridade, string> = {
  ALTA: 'Alta',
  MEDIA: 'Média',
  BAIXA: 'Baixa',
}

/** Um encontro pós-encontro da paróquia, identificado pela data (RN-POS-01). */
export interface EncontroPos {
  id: string
  paroquiaId: string
  paroquiaNome: string
  data: string
  tema: string | null
  createdAt: string
}

export interface AgendarEncontroPosPayload {
  paroquiaId: string
  data: string
  tema?: string
}

/** Linha da lista de chamada. `presente` nulo = presença ainda não conferida. */
export interface Presenca {
  pessoaId: string
  pessoaNome: string
  situacao: string
  presente: boolean | null
}

export interface RegistrarPresencasPayload {
  encontroPosId: string
  presencas: { pessoaId: string; presente: boolean }[]
}

/**
 * Frequência apurada de um acompanhado e a faixa de prioridade resultante (RF-059, RN-POS-03).
 * `totalEncontros` é quantos encontros pós a paróquia realizou no período — o denominador comum do
 * ranking, e não o número de registros da pessoa.
 */
export interface FrequenciaPessoa {
  pessoaId: string
  pessoaNome: string
  totalEncontros: number
  presencas: number
  percentual: number
  faixaPrioridade: FaixaPrioridade
}

export interface FiltroFrequencia {
  paroquiaId: string
  de?: string
  ate?: string
}
