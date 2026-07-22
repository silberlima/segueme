import { useQuery } from '@tanstack/react-query'
import { fetchHistoricoPessoa } from '../services/pessoa.service'

export function useHistoricoPessoa(id: string | undefined) {
  return useQuery({
    queryKey: ['pessoa-historico', id],
    queryFn: () => fetchHistoricoPessoa(id as string),
    enabled: Boolean(id),
  })
}
