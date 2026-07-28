import { useQuery } from '@tanstack/react-query'
import { fetchConvites } from '../services/montagem.service'
import type { PapelIntegrante, StatusIntegrante } from '../domain/montagem.types'

export function useConvites(
  encontroId: string | undefined,
  filtros?: { status?: StatusIntegrante; equipeId?: string; papel?: PapelIntegrante },
) {
  return useQuery({
    queryKey: ['convites', encontroId, filtros],
    queryFn: () => fetchConvites(encontroId as string, filtros),
    enabled: Boolean(encontroId),
  })
}
