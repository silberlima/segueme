import { useQuery } from '@tanstack/react-query'
import { fetchPrioridades } from '../services/posencontro.service'

export function usePrioridades(paroquiaId: string) {
  return useQuery({
    queryKey: ['pos-encontro', 'prioridades', paroquiaId],
    queryFn: () => fetchPrioridades(paroquiaId),
    enabled: Boolean(paroquiaId),
  })
}
