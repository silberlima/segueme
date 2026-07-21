import { httpClient } from '../../../shared/utils/httpClient'
import type { Pagination } from '../../../shared/types/pagination.types'
import type { Notificacao } from '../domain/notificacao.types'

export async function fetchNotificacoes(page = 0, size = 20): Promise<Pagination<Notificacao>> {
  const { data } = await httpClient.get<Pagination<Notificacao>>('/notificacoes', { params: { page, size } })
  return data
}

export async function marcarNotificacoesComoLidas(): Promise<void> {
  await httpClient.patch('/notificacoes/marcar-lidas')
}
