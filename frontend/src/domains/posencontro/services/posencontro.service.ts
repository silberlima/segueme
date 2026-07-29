import { httpClient } from '../../../shared/utils/httpClient'
import type { Pagination } from '../../../shared/types/pagination.types'
import type {
  AgendarEncontroPosPayload,
  EncontroPos,
  FiltroFrequencia,
  FrequenciaPessoa,
  Presenca,
  RegistrarPresencasPayload,
} from '../domain/posencontro.types'

export interface ParoquiaOption {
  id: string
  nome: string
}

export async function fetchParoquiaOptions(): Promise<ParoquiaOption[]> {
  const { data } = await httpClient.get<Pagination<ParoquiaOption>>('/paroquias', { params: { size: 200 } })
  return data.content
}

export async function fetchEncontrosPos(paroquiaId: string): Promise<EncontroPos[]> {
  const { data } = await httpClient.get<Pagination<EncontroPos>>('/encontros-pos', {
    params: { paroquiaId, size: 100 },
  })
  return data.content
}

export async function agendarEncontroPos(payload: AgendarEncontroPosPayload): Promise<EncontroPos> {
  const { data } = await httpClient.post<EncontroPos>('/encontros-pos', payload)
  return data
}

export async function fetchPresencas(encontroPosId: string): Promise<Presenca[]> {
  const { data } = await httpClient.get<Presenca[]>(`/encontros-pos/${encontroPosId}/presencas`)
  return data
}

export async function registrarPresencas({
  encontroPosId,
  presencas,
}: RegistrarPresencasPayload): Promise<Presenca[]> {
  const { data } = await httpClient.put<Presenca[]>(`/encontros-pos/${encontroPosId}/presencas`, { presencas })
  return data
}

export async function fetchFrequencia(filtro: FiltroFrequencia): Promise<FrequenciaPessoa[]> {
  const { data } = await httpClient.get<FrequenciaPessoa[]>('/pos-encontro/frequencia', { params: filtro })
  return data
}

export async function fetchPrioridades(paroquiaId: string): Promise<FrequenciaPessoa[]> {
  const { data } = await httpClient.get<FrequenciaPessoa[]>('/pos-encontro/prioridades', { params: { paroquiaId } })
  return data
}
