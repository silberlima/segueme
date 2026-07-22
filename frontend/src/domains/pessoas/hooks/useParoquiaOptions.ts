import { useQuery } from '@tanstack/react-query'
import { fetchParoquiaOptions } from '../services/pessoa.service'

export function useParoquiaOptions() {
  return useQuery({
    queryKey: ['paroquia-options'],
    queryFn: fetchParoquiaOptions,
  })
}
