import { useQuery } from '@tanstack/react-query'
import { fetchPessoa } from '../services/pessoa.service'

export function usePessoa(id: string | undefined) {
  return useQuery({
    queryKey: ['pessoa', id],
    queryFn: () => fetchPessoa(id as string),
    enabled: Boolean(id),
  })
}
