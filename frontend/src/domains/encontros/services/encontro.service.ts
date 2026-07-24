import { httpClient } from '../../../shared/utils/httpClient'
import type { Pagination } from '../../../shared/types/pagination.types'
import type { Encontro, EncontroResumo, NovoEncontroPayload, StatusEncontro } from '../domain/encontro.types'

export interface FiltrosEncontros {
  numero?: number
  status?: StatusEncontro
  paroquiaId?: string
}

export async function fetchEncontros(
  filtros: FiltrosEncontros = {},
  page = 0,
  size = 20,
): Promise<Pagination<EncontroResumo>> {
  const { data } = await httpClient.get<Pagination<EncontroResumo>>('/encontros', {
    params: { ...filtros, page, size },
  })
  return data
}

export async function fetchEncontro(id: string): Promise<Encontro> {
  const { data } = await httpClient.get<Encontro>(`/encontros/${id}`)
  return data
}

export async function createEncontro(payload: NovoEncontroPayload): Promise<Encontro> {
  const { data } = await httpClient.post<Encontro>('/encontros', payload)
  return data
}

export interface PessoaOption {
  id: string
  nomeCompleto: string
}

/**
 * Busca de Pessoa para os seletores do wizard "Criar Encontro" (diretor espiritual, membros da
 * Equipe Dirigente, coordenador geral), via o contrato REST de `/api/v1/pessoas` — não importa
 * de `domains/pessoas` diretamente (`skills/padrao-componentes-react-tailwind.md` §3).
 */
export async function searchPessoas(nome: string, paroquiaId?: string): Promise<PessoaOption[]> {
  if (!nome.trim()) {
    return []
  }
  const { data } = await httpClient.get<Pagination<PessoaOption>>('/pessoas', {
    params: { nome, paroquiaId, size: 10 },
  })
  return data.content
}

export interface CasalOption {
  id: string
  pessoa1: { nomeCompleto: string }
  pessoa2: { nomeCompleto: string }
}

/** Opções de Casal para os seletores do wizard, via o contrato REST de `/api/v1/casais`. */
export async function fetchCasalOptions(): Promise<CasalOption[]> {
  const { data } = await httpClient.get<Pagination<CasalOption>>('/casais', { params: { size: 200 } })
  return data.content
}
