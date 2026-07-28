import { useQuery } from '@tanstack/react-query'
import { fetchSugestoes } from '../services/montagem.service'

export function useSugestoes(encontroId: string | undefined, equipeId: string | undefined) {
  return useQuery({
    queryKey: ['sugestoes', encontroId, equipeId],
    queryFn: () => fetchSugestoes(encontroId as string, equipeId as string),
    enabled: Boolean(encontroId) && Boolean(equipeId),
  })
}
