import { useQuery } from '@tanstack/react-query'
import { fetchPresencas } from '../services/posencontro.service'

export function usePresencas(encontroPosId: string) {
  return useQuery({
    queryKey: ['presencas', encontroPosId],
    queryFn: () => fetchPresencas(encontroPosId),
    enabled: Boolean(encontroPosId),
  })
}
