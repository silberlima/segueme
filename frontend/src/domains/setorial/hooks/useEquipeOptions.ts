import { useQuery } from '@tanstack/react-query'
import { fetchEquipeOptions } from '../services/setorial.service'

export function useEquipeOptions(encontroId: string | undefined) {
  return useQuery({
    queryKey: ['equipe-options-setorial', encontroId],
    queryFn: () => fetchEquipeOptions(encontroId as string),
    enabled: Boolean(encontroId),
  })
}
