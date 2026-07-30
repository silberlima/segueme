import { httpClient } from '../../../shared/utils/httpClient'
import type { Pagination } from '../../../shared/types/pagination.types'
import type { Notificacao, PreferenciaNotificacao } from '../domain/notificacao.types'

export async function fetchNotificacoes(page = 0, size = 20): Promise<Pagination<Notificacao>> {
  const { data } = await httpClient.get<Pagination<Notificacao>>('/notificacoes', { params: { page, size } })
  return data
}

/** RN-SISTEMA-02: o badge conta no banco, não na página que o drawer trouxe. */
export async function fetchNotificacoesNaoLidas(): Promise<number> {
  const { data } = await httpClient.get<{ total: number }>('/notificacoes/nao-lidas')
  return data.total
}

export async function marcarNotificacoesComoLidas(): Promise<void> {
  await httpClient.patch('/notificacoes/marcar-lidas')
}

export async function fetchPreferenciasDeNotificacao(): Promise<PreferenciaNotificacao[]> {
  const { data } = await httpClient.get<PreferenciaNotificacao[]>('/notificacoes/preferencias')
  return data
}

export async function salvarPreferenciasDeNotificacao(
  preferencias: PreferenciaNotificacao[],
): Promise<PreferenciaNotificacao[]> {
  const { data } = await httpClient.put<PreferenciaNotificacao[]>('/notificacoes/preferencias', { preferencias })
  return data
}
