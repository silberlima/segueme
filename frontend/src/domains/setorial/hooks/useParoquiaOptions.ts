import { useQuery } from '@tanstack/react-query'
import { fetchParoquiaOptions } from '../services/setorial.service'

export function useParoquiaOptions() {
  return useQuery({
    queryKey: ['paroquia-options-setorial'],
    queryFn: fetchParoquiaOptions,
  })
}
