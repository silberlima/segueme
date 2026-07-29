export type TipoLancamento = 'RECEITA' | 'DESPESA'

export const TIPO_LANCAMENTO_LABEL: Record<TipoLancamento, string> = {
  RECEITA: 'Receita',
  DESPESA: 'Despesa',
}

export type SituacaoDespesa = 'PENDENTE' | 'APROVADA' | 'PAGA'

export const SITUACAO_DESPESA_LABEL: Record<SituacaoDespesa, string> = {
  PENDENTE: 'Pendente',
  APROVADA: 'Aprovada',
  PAGA: 'Paga',
}

export type StatusCampanha = 'ATIVA' | 'ENCERRADA'

export const STATUS_CAMPANHA_LABEL: Record<StatusCampanha, string> = {
  ATIVA: 'Ativa',
  ENCERRADA: 'Encerrada',
}

/** Receita ou despesa de um Encontro (RF-047 a RF-049, RN-FINANCEIRO-01/02). */
export interface LancamentoFinanceiro {
  id: string
  encontroId: string
  encontroNumero: number
  tipo: TipoLancamento
  categoria: string
  valor: number
  data: string
  situacao: SituacaoDespesa | null
  aprovadoPor: string | null
  createdAt: string
}

export interface RegistrarLancamentoPayload {
  encontroId: string
  tipo: TipoLancamento
  categoria: string
  valor: number
  data: string
}

/** Campanha de arrecadação de um Encontro (RF-050, RN-FINANCEIRO-03). */
export interface Campanha {
  id: string
  encontroId: string
  encontroNumero: number
  nome: string
  meta: number
  valorArrecadado: number
  status: StatusCampanha
  createdAt: string
}

export interface CriarCampanhaPayload {
  encontroId: string
  nome: string
  meta: number
}

/** Saúde financeira consolidada de um Encontro (RN-FINANCEIRO-04). */
export interface ResumoFinanceiro {
  saldo: number
  totalReceitas: number
  totalDespesas: number
  campanhasAtivas: number
}
