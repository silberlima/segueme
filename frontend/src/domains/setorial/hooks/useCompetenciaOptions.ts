import { useQuery } from '@tanstack/react-query'
import { fetchCompetenciaOptions } from '../services/setorial.service'

export function useCompetenciaOptions() {
  return useQuery({
    queryKey: ['competencia-options-setorial'],
    queryFn: fetchCompetenciaOptions,
  })
}
