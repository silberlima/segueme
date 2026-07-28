import { httpClient } from '../../../shared/utils/httpClient'
import type { Pagination } from '../../../shared/types/pagination.types'
import type {
  CandidatoElegivel,
  Emprestimo,
  Solicitacao,
  SolicitarEmprestimoPayload,
  SolicitarTransferenciaPayload,
  StatusEmprestimo,
  StatusTransferencia,
  Transferencia,
} from '../domain/setorial.types'

export async function fetchTransferencias(
  filtros?: { status?: StatusTransferencia; pessoaId?: string },
  page = 0,
  size = 20,
): Promise<Pagination<Transferencia>> {
  const { data } = await httpClient.get<Pagination<Transferencia>>('/transferencias', {
    params: { ...filtros, page, size },
  })
  return data
}

export async function solicitarTransferencia(payload: SolicitarTransferenciaPayload): Promise<Transferencia> {
  const { data } = await httpClient.post<Transferencia>('/transferencias', payload)
  return data
}

export async function aprovarTransferencia(transferenciaId: string): Promise<Transferencia> {
  const { data } = await httpClient.patch<Transferencia>(`/transferencias/${transferenciaId}/aprovar`)
  return data
}

export async function recusarTransferencia(transferenciaId: string, justificativa: string): Promise<Transferencia> {
  const { data } = await httpClient.patch<Transferencia>(`/transferencias/${transferenciaId}/recusar`, { justificativa })
  return data
}

export async function fetchEmprestimos(filtros?: {
  encontroId?: string
  equipeId?: string
  status?: StatusEmprestimo
}): Promise<Emprestimo[]> {
  const { data } = await httpClient.get<Emprestimo[]>('/emprestimos', { params: filtros })
  return data
}

export async function solicitarEmprestimo(payload: SolicitarEmprestimoPayload): Promise<Emprestimo> {
  const { data } = await httpClient.post<Emprestimo>('/emprestimos', payload)
  return data
}

export async function fetchCandidatosEmprestimo(
  emprestimoId: string,
  filtros?: { paroquiaId?: string; competenciaId?: string },
): Promise<CandidatoElegivel[]> {
  const { data } = await httpClient.get<CandidatoElegivel[]>(`/emprestimos/${emprestimoId}/candidatos`, {
    params: filtros,
  })
  return data
}

export async function enviarCandidatos(emprestimoId: string, pessoaIds: string[]): Promise<Emprestimo> {
  const { data } = await httpClient.patch<Emprestimo>(`/emprestimos/${emprestimoId}/enviar-candidatos`, { pessoaIds })
  return data
}

export async function efetivarEmprestimo(emprestimoId: string): Promise<Emprestimo> {
  const { data } = await httpClient.patch<Emprestimo>(`/emprestimos/${emprestimoId}/efetivar`)
  return data
}

export async function recusarEmprestimo(emprestimoId: string, justificativa: string): Promise<Emprestimo> {
  const { data } = await httpClient.patch<Emprestimo>(`/emprestimos/${emprestimoId}/recusar`, { justificativa })
  return data
}

export async function fetchSolicitacoes(): Promise<Solicitacao[]> {
  const { data } = await httpClient.get<Solicitacao[]>('/solicitacoes')
  return data
}

export interface ParoquiaOption {
  id: string
  nome: string
}

/** Opções de Paróquia para o seletor de destino da Transferência, via o contrato REST de `/api/v1/paroquias`. */
export async function fetchParoquiaOptions(): Promise<ParoquiaOption[]> {
  const { data } = await httpClient.get<Pagination<ParoquiaOption>>('/paroquias', { params: { size: 200 } })
  return data.content
}

export interface EncontroOption {
  id: string
  numero: number
  padroeiro: string
}

/** Opções de Encontro para o seletor de "Nova solicitação" de Empréstimo, via `/api/v1/encontros`. */
export async function fetchEncontroOptions(): Promise<EncontroOption[]> {
  const { data } = await httpClient.get<Pagination<EncontroOption>>('/encontros', { params: { size: 100 } })
  return data.content
}

export interface EquipeOption {
  id: string
  nome: string
}

/** Opções de Equipe de um Encontro, via `/api/v1/equipes`. */
export async function fetchEquipeOptions(encontroId: string): Promise<EquipeOption[]> {
  const { data } = await httpClient.get<EquipeOption[]>('/equipes', { params: { encontroId } })
  return data
}

export interface CompetenciaOption {
  id: string
  nome: string
}

/** Opções de Competência para o filtro de candidatos e a habilidade requerida, via `/api/v1/competencias`. */
export async function fetchCompetenciaOptions(): Promise<CompetenciaOption[]> {
  const { data } = await httpClient.get<CompetenciaOption[]>('/competencias')
  return data
}

export interface PessoaOption {
  id: string
  nomeCompleto: string
}

/** Busca de Pessoa por nome, para o seletor de "quem transferir", via o contrato REST de `/api/v1/pessoas`. */
export async function searchPessoas(nome: string): Promise<PessoaOption[]> {
  if (!nome.trim()) {
    return []
  }
  const { data } = await httpClient.get<Pagination<PessoaOption>>('/pessoas', { params: { nome, size: 10 } })
  return data.content
}
