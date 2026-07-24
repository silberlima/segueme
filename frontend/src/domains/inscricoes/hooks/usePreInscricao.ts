import { useQuery } from '@tanstack/react-query'
import { fetchPreInscricao } from '../services/inscricao.service'

export function usePreInscricao(id: string | undefined) {
  return useQuery({
    queryKey: ['pre-inscricao', id],
    queryFn: () => fetchPreInscricao(id as string),
    enabled: Boolean(id),
  })
}
