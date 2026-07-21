import { useQuery } from '@tanstack/react-query'
import { fetchParoquias } from '../services/hierarquia.service'

export function useParoquias(page = 0, size = 20) {
  return useQuery({
    queryKey: ['paroquias', page, size],
    queryFn: () => fetchParoquias(page, size),
  })
}
