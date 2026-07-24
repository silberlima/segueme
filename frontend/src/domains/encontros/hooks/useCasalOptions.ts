import { useQuery } from '@tanstack/react-query'
import { fetchCasalOptions } from '../services/encontro.service'

export function useCasalOptions() {
  return useQuery({
    queryKey: ['casal-options'],
    queryFn: fetchCasalOptions,
  })
}
