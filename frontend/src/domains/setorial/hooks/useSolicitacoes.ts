import { useQuery } from '@tanstack/react-query'
import { fetchSolicitacoes } from '../services/setorial.service'

export function useSolicitacoes() {
  return useQuery({
    queryKey: ['solicitacoes'],
    queryFn: fetchSolicitacoes,
  })
}
