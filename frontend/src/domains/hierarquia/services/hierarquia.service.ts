import { httpClient } from '../../../shared/utils/httpClient'
import type { Pagination } from '../../../shared/types/pagination.types'
import type { Diocese, Paroquia } from '../domain/hierarquia.types'

export async function fetchDioceses(page = 0, size = 20): Promise<Pagination<Diocese>> {
  const { data } = await httpClient.get<Pagination<Diocese>>('/dioceses', { params: { page, size } })
  return data
}

export async function fetchParoquias(page = 0, size = 20): Promise<Pagination<Paroquia>> {
  const { data } = await httpClient.get<Pagination<Paroquia>>('/paroquias', { params: { page, size } })
  return data
}
