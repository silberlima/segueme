import { useQuery } from '@tanstack/react-query'
import { fetchResumoFinanceiro } from '../services/financeiro.service'

export function useResumoFinanceiro(encontroId: string) {
  return useQuery({
    queryKey: ['financeiro', 'resumo', encontroId],
    queryFn: () => fetchResumoFinanceiro(encontroId),
  })
}
