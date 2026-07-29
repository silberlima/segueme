import { useQuery } from '@tanstack/react-query'
import { fetchParoquiaOptions } from '../services/posencontro.service'

export function useParoquiaOptions() {
  return useQuery({
    queryKey: ['paroquia-options-posencontro'],
    queryFn: fetchParoquiaOptions,
  })
}
