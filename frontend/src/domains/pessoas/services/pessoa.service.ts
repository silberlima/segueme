import { httpClient } from '../../../shared/utils/httpClient'
import type { Pagination } from '../../../shared/types/pagination.types'
import type {
  Casal,
  Competencia,
  HistoricoEvento,
  NovaPessoaPayload,
  NovoCasalPayload,
  Pessoa,
  PessoaResumo,
} from '../domain/pessoa.types'

export interface FiltrosPessoas {
  nome?: string
  paroquiaId?: string
  competenciaId?: string
}

export async function fetchPessoas(
  filtros: FiltrosPessoas = {},
  page = 0,
  size = 20,
): Promise<Pagination<PessoaResumo>> {
  const { data } = await httpClient.get<Pagination<PessoaResumo>>('/pessoas', {
    params: { ...filtros, page, size },
  })
  return data
}

export async function fetchPessoa(id: string): Promise<Pessoa> {
  const { data } = await httpClient.get<Pessoa>(`/pessoas/${id}`)
  return data
}

export async function createPessoa(payload: NovaPessoaPayload): Promise<Pessoa> {
  const { data } = await httpClient.post<Pessoa>('/pessoas', payload)
  return data
}

export async function fetchHistoricoPessoa(id: string): Promise<HistoricoEvento[]> {
  const { data } = await httpClient.get<HistoricoEvento[]>(`/pessoas/${id}/historico`)
  return data
}

export async function fetchCasais(page = 0, size = 20): Promise<Pagination<Casal>> {
  const { data } = await httpClient.get<Pagination<Casal>>('/casais', { params: { page, size } })
  return data
}

export async function createCasal(payload: NovoCasalPayload): Promise<Casal> {
  const { data } = await httpClient.post<Casal>('/casais', payload)
  return data
}

export async function fetchCompetencias(): Promise<Competencia[]> {
  const { data } = await httpClient.get<Competencia[]>('/competencias')
  return data
}

export async function fetchPessoasPorCompetencia(
  competenciaId: string,
  page = 0,
  size = 20,
): Promise<Pagination<PessoaResumo>> {
  const { data } = await httpClient.get<Pagination<PessoaResumo>>(`/competencias/${competenciaId}/pessoas`, {
    params: { page, size },
  })
  return data
}

export interface ParoquiaOption {
  id: string
  nome: string
}

/**
 * Opções de paróquia para o formulário de Cadastro de Pessoa, via o contrato REST de
 * `/api/v1/paroquias` (Fase 2) — não importa de `domains/hierarquia` diretamente
 * (`skills/padrao-componentes-react-tailwind.md` §3: domínios só se comunicam por contrato).
 */
export async function fetchParoquiaOptions(): Promise<ParoquiaOption[]> {
  const { data } = await httpClient.get<Pagination<ParoquiaOption>>('/paroquias', {
    params: { size: 200 },
  })
  return data.content
}
