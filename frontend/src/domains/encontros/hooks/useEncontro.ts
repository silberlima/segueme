import { useQuery } from '@tanstack/react-query'
import { fetchEncontro } from '../services/encontro.service'

export function useEncontro(id: string | undefined) {
  return useQuery({
    queryKey: ['encontro', id],
    queryFn: () => fetchEncontro(id as string),
    enabled: Boolean(id),
  })
}
