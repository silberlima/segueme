import { httpClient } from '../../../shared/utils/httpClient'
import type { Pagination } from '../../../shared/types/pagination.types'
import type {
  CadastroReserva,
  CriarPreInscricaoResultado,
  EncontroContexto,
  FichaInscricao,
  Funil,
  NovaPreInscricaoPayload,
  Participante,
  PessoaCompleta,
  PreInscricao,
  StatusPagamento,
  StatusPreInscricao,
} from '../domain/inscricao.types'

export interface FiltrosPreInscricoes {
  encontroId?: string
  status?: StatusPreInscricao
  nome?: string
}

export async function fetchPreInscricoes(
  filtros: FiltrosPreInscricoes = {},
  page = 0,
  size = 20,
): Promise<Pagination<PreInscricao>> {
  const { data } = await httpClient.get<Pagination<PreInscricao>>('/pre-inscricoes', {
    params: { ...filtros, page, size },
  })
  return data
}

export async function criarPreInscricao(payload: NovaPreInscricaoPayload): Promise<CriarPreInscricaoResultado> {
  const { data } = await httpClient.post<CriarPreInscricaoResultado>('/pre-inscricoes', payload)
  return data
}

export async function marcarEmAnalise(preInscricaoId: string): Promise<PreInscricao> {
  const { data } = await httpClient.patch<PreInscricao>(`/pre-inscricoes/${preInscricaoId}/em-analise`)
  return data
}

export async function preencherFicha(preInscricaoId: string): Promise<FichaInscricao> {
  const { data } = await httpClient.post<FichaInscricao>(`/pre-inscricoes/${preInscricaoId}/ficha`)
  return data
}

export async function fetchPreInscricao(id: string): Promise<PreInscricao> {
  const { data } = await httpClient.get<PreInscricao>(`/pre-inscricoes/${id}`)
  return data
}

export async function fetchCadastroReserva(encontroId: string): Promise<CadastroReserva[]> {
  const { data } = await httpClient.get<CadastroReserva[]>('/cadastro-reserva', { params: { encontroId } })
  return data
}

export async function promoverReserva(cadastroReservaId: string, encontroDestinoId: string): Promise<PreInscricao> {
  const { data } = await httpClient.post<PreInscricao>(`/cadastro-reserva/${cadastroReservaId}/promover`, {
    encontroDestinoId,
  })
  return data
}

export async function fetchParticipantes(
  encontroId: string,
  page = 0,
  size = 20,
): Promise<Pagination<Participante>> {
  const { data } = await httpClient.get<Pagination<Participante>>('/participantes', {
    params: { encontroId, page, size },
  })
  return data
}

export async function fetchFunil(encontroId: string): Promise<Funil> {
  const { data } = await httpClient.get<Funil>('/participantes/funil', { params: { encontroId } })
  return data
}

export async function atualizarPagamento(
  fichaInscricaoId: string,
  statusPagamento: StatusPagamento,
): Promise<Participante> {
  const { data } = await httpClient.patch<Participante>(`/participantes/${fichaInscricaoId}/pagamento`, {
    statusPagamento,
  })
  return data
}

export interface PessoaOption {
  id: string
  nomeCompleto: string
}

/**
 * Busca de Pessoa já cadastrada para a "Nova pré-inscrição", via o contrato REST de
 * `/api/v1/pessoas` — não importa de `domains/pessoas` diretamente (mesmo racional do
 * `PersonSearchSelect` em `domains/encontros`, `skills/padrao-componentes-react-tailwind.md` §3).
 */
export async function searchPessoas(nome: string): Promise<PessoaOption[]> {
  if (!nome.trim()) {
    return []
  }
  const { data } = await httpClient.get<Pagination<PessoaOption>>('/pessoas', {
    params: { nome, size: 10 },
  })
  return data.content
}

/** Perfil completo de uma Pessoa, para exibir as 4 seções da Ficha de Inscrição (Fase 3). */
export async function fetchPessoaCompleta(pessoaId: string): Promise<PessoaCompleta> {
  const { data } = await httpClient.get<PessoaCompleta>(`/pessoas/${pessoaId}`)
  return data
}

/** Contexto mínimo do Encontro (vagas, número, padroeiro), via o contrato REST de `/api/v1/encontros`. */
export async function fetchEncontroContexto(encontroId: string): Promise<EncontroContexto> {
  const { data } = await httpClient.get<EncontroContexto>(`/encontros/${encontroId}`)
  return data
}
