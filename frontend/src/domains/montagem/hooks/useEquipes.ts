import { useQuery } from '@tanstack/react-query'
import { fetchEquipes } from '../services/montagem.service'

export function useEquipes(encontroId: string | undefined) {
  return useQuery({
    queryKey: ['equipes', encontroId],
    queryFn: () => fetchEquipes(encontroId as string),
    enabled: Boolean(encontroId),
  })
}
