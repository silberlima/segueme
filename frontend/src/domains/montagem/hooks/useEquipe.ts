import { useQuery } from '@tanstack/react-query'
import { fetchEquipe } from '../services/montagem.service'

export function useEquipe(equipeId: string | undefined, encontroId: string | undefined) {
  return useQuery({
    queryKey: ['equipe', equipeId, encontroId],
    queryFn: () => fetchEquipe(equipeId as string, encontroId as string),
    enabled: Boolean(equipeId) && Boolean(encontroId),
  })
}
