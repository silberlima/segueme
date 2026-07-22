import { useQuery } from '@tanstack/react-query'
import { fetchCompetencias } from '../services/pessoa.service'

export function useCompetencias() {
  return useQuery({
    queryKey: ['competencias'],
    queryFn: fetchCompetencias,
  })
}
