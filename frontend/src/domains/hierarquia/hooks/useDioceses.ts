import { useQuery } from '@tanstack/react-query'
import { fetchDioceses } from '../services/hierarquia.service'

export function useDioceses(page = 0, size = 20) {
  return useQuery({
    queryKey: ['dioceses', page, size],
    queryFn: () => fetchDioceses(page, size),
  })
}
