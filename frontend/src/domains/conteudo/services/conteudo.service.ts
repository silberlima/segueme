import { httpClient } from '../../../shared/utils/httpClient'
import type { Pagination } from '../../../shared/types/pagination.types'
import type { CadastrarPalestrantePayload, Palestrante, PalestraDoEncontro, TipoPalestrante } from '../domain/conteudo.types'

export async function fetchPalestras(encontroId: string): Promise<PalestraDoEncontro[]> {
  const { data } = await httpClient.get<PalestraDoEncontro[]>(`/encontros/${encontroId}/palestras`)
  return data
}

export async function designarPalestrante(
  encontroId: string,
  palestraId: string,
  palestranteId: string,
): Promise<PalestraDoEncontro> {
  const { data } = await httpClient.patch<PalestraDoEncontro>(
    `/encontros/${encontroId}/palestras/${palestraId}/designar`,
    { palestranteId },
  )
  return data
}

export async function fetchPalestrantes(filtros?: { tipo?: TipoPalestrante }): Promise<Pagination<Palestrante>> {
  const { data } = await httpClient.get<Pagination<Palestrante>>('/palestrantes', { params: { ...filtros, size: 100 } })
  return data
}

export async function cadastrarPalestrante(payload: CadastrarPalestrantePayload): Promise<Palestrante> {
  const { data } = await httpClient.post<Palestrante>('/palestrantes', payload)
  return data
}

export interface PessoaOption {
  id: string
  nomeCompleto: string
}

export async function searchPessoas(nome: string): Promise<PessoaOption[]> {
  if (!nome.trim()) {
    return []
  }
  const { data } = await httpClient.get<Pagination<PessoaOption>>('/pessoas', { params: { nome, size: 10 } })
  return data.content
}
