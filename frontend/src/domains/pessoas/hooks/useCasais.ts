import { useQuery } from '@tanstack/react-query'
import { fetchCasais } from '../services/pessoa.service'

export function useCasais(page = 0, size = 20) {
  return useQuery({
    queryKey: ['casais', page, size],
    queryFn: () => fetchCasais(page, size),
  })
}
