import { httpClient } from '../../../shared/utils/httpClient'
import type { Pagination } from '../../../shared/types/pagination.types'
import type {
  CandidatoSugerido,
  EquipeDetalhe,
  EquipeResumo,
  NovaSubstituicaoPayload,
  PapelIntegrante,
  Substituicao,
} from '../domain/montagem.types'

export async function fetchEquipes(encontroId: string): Promise<EquipeResumo[]> {
  const { data } = await httpClient.get<EquipeResumo[]>('/equipes', { params: { encontroId } })
  return data
}

export async function fetchEquipe(equipeId: string, encontroId: string): Promise<EquipeDetalhe> {
  const { data } = await httpClient.get<EquipeDetalhe>(`/equipes/${equipeId}`, { params: { encontroId } })
  return data
}

export async function fetchSugestoes(encontroId: string, equipeId: string): Promise<CandidatoSugerido[]> {
  const { data } = await httpClient.get<CandidatoSugerido[]>('/sugestoes', { params: { encontroId, equipeId } })
  return data
}

export async function adicionarIntegrante(
  equipeId: string,
  encontroId: string,
  pessoaId: string,
  papel: PapelIntegrante,
): Promise<void> {
  await httpClient.post(`/equipes/${equipeId}/integrantes`, { pessoaId, papel }, { params: { encontroId } })
}

export async function fetchSubstituicoes(
  encontroId: string,
  equipeId?: string,
  page = 0,
  size = 20,
): Promise<Pagination<Substituicao>> {
  const { data } = await httpClient.get<Pagination<Substituicao>>('/substituicoes', {
    params: { encontroId, equipeId, page, size },
  })
  return data
}

export async function registrarSubstituicao(payload: NovaSubstituicaoPayload): Promise<Substituicao> {
  const { data } = await httpClient.post<Substituicao>('/substituicoes', payload)
  return data
}

export interface PessoaOption {
  id: string
  nomeCompleto: string
}

/** Busca de Pessoa por nome para "quem entra" na substituição, via o contrato REST de `/api/v1/pessoas`. */
export async function searchPessoas(nome: string): Promise<PessoaOption[]> {
  if (!nome.trim()) {
    return []
  }
  const { data } = await httpClient.get<Pagination<PessoaOption>>('/pessoas', { params: { nome, size: 10 } })
  return data.content
}
