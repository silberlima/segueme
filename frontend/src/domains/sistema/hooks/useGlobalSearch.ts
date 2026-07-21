import { useQuery } from '@tanstack/react-query'
import { searchGlobal } from '../services/search.service'

const MIN_QUERY_LENGTH = 2

export function useGlobalSearch(query: string) {
  const trimmed = query.trim()

  return useQuery({
    queryKey: ['search', trimmed],
    queryFn: () => searchGlobal(trimmed),
    enabled: trimmed.length >= MIN_QUERY_LENGTH,
  })
}
