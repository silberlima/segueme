import { useQuery } from '@tanstack/react-query'
import { fetchEncontrosPos } from '../services/posencontro.service'

export function useEncontrosPos(paroquiaId: string) {
  return useQuery({
    queryKey: ['encontros-pos', paroquiaId],
    queryFn: () => fetchEncontrosPos(paroquiaId),
    enabled: Boolean(paroquiaId),
  })
}
