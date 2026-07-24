import { useQuery } from '@tanstack/react-query'
import { fetchParticipantes } from '../services/inscricao.service'

export function useParticipantes(encontroId: string | undefined, page = 0, size = 20) {
  return useQuery({
    queryKey: ['participantes', encontroId, page, size],
    queryFn: () => fetchParticipantes(encontroId as string, page, size),
    enabled: Boolean(encontroId),
  })
}
