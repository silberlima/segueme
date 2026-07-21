import { httpClient } from '../../../shared/utils/httpClient'
import type { MeResponse } from '../domain/me.types'

export async function fetchMe(): Promise<MeResponse> {
  const { data } = await httpClient.get<MeResponse>('/me')
  return data
}
