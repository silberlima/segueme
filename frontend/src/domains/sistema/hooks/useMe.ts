import { useQuery } from '@tanstack/react-query'
import { fetchMe } from '../services/me.service'

export function useMe() {
  return useQuery({
    queryKey: ['me'],
    queryFn: fetchMe,
    staleTime: 5 * 60_000,
  })
}
