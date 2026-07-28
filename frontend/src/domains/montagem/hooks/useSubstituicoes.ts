import { useQuery } from '@tanstack/react-query'
import { fetchSubstituicoes } from '../services/montagem.service'

export function useSubstituicoes(encontroId: string | undefined, equipeId?: string, page = 0) {
  return useQuery({
    queryKey: ['substituicoes', encontroId, equipeId, page],
    queryFn: () => fetchSubstituicoes(encontroId as string, equipeId, page),
    enabled: Boolean(encontroId),
  })
}
