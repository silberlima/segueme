import { useQuery } from '@tanstack/react-query'
import { fetchFunil } from '../services/inscricao.service'

export function useFunil(encontroId: string | undefined) {
  return useQuery({
    queryKey: ['funil', encontroId],
    queryFn: () => fetchFunil(encontroId as string),
    enabled: Boolean(encontroId),
  })
}
