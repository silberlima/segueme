import { httpClient } from '../../../shared/utils/httpClient'
import type { Pagination } from '../../../shared/types/pagination.types'
import type {
  Campanha,
  CriarCampanhaPayload,
  LancamentoFinanceiro,
  RegistrarLancamentoPayload,
  ResumoFinanceiro,
  SituacaoDespesa,
  StatusCampanha,
  TipoLancamento,
} from '../domain/financeiro.types'

export async function fetchResumoFinanceiro(encontroId: string): Promise<ResumoFinanceiro> {
  const { data } = await httpClient.get<ResumoFinanceiro>('/financeiro/resumo', { params: { encontroId } })
  return data
}

export async function fetchLancamentos(filtros: {
  encontroId: string
  tipo?: TipoLancamento
  situacao?: SituacaoDespesa
}): Promise<Pagination<LancamentoFinanceiro>> {
  const { data } = await httpClient.get<Pagination<LancamentoFinanceiro>>('/lancamentos-financeiros', {
    params: { ...filtros, size: 100 },
  })
  return data
}

export async function registrarLancamento(payload: RegistrarLancamentoPayload): Promise<LancamentoFinanceiro> {
  const { data } = await httpClient.post<LancamentoFinanceiro>('/lancamentos-financeiros', payload)
  return data
}

export async function aprovarLancamento(lancamentoId: string): Promise<LancamentoFinanceiro> {
  const { data } = await httpClient.patch<LancamentoFinanceiro>(`/lancamentos-financeiros/${lancamentoId}/aprovar`)
  return data
}

export async function pagarLancamento(lancamentoId: string): Promise<LancamentoFinanceiro> {
  const { data } = await httpClient.patch<LancamentoFinanceiro>(`/lancamentos-financeiros/${lancamentoId}/pagar`)
  return data
}

export async function fetchCampanhas(filtros: { encontroId: string; status?: StatusCampanha }): Promise<Pagination<Campanha>> {
  const { data } = await httpClient.get<Pagination<Campanha>>('/campanhas', { params: { ...filtros, size: 100 } })
  return data
}

export async function criarCampanha(payload: CriarCampanhaPayload): Promise<Campanha> {
  const { data } = await httpClient.post<Campanha>('/campanhas', payload)
  return data
}

export async function registrarArrecadacao(campanhaId: string, valor: number): Promise<Campanha> {
  const { data } = await httpClient.patch<Campanha>(`/campanhas/${campanhaId}/arrecadacao`, { valor })
  return data
}

export async function encerrarCampanha(campanhaId: string): Promise<Campanha> {
  const { data } = await httpClient.patch<Campanha>(`/campanhas/${campanhaId}/encerrar`)
  return data
}
