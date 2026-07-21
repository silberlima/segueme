import { httpClient } from '../../../shared/utils/httpClient'
import type { BuscaResultado } from '../domain/busca.types'

export async function searchGlobal(query: string): Promise<BuscaResultado[]> {
  const { data } = await httpClient.get<BuscaResultado[]>('/search', { params: { q: query } })
  return data
}
