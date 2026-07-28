import { useQuery } from '@tanstack/react-query'
import { fetchEncontroOptions } from '../services/setorial.service'

export function useEncontroOptions() {
  return useQuery({
    queryKey: ['encontro-options-setorial'],
    queryFn: fetchEncontroOptions,
  })
}
