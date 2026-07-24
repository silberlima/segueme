import { useQuery } from '@tanstack/react-query'
import { fetchPessoaCompleta } from '../services/inscricao.service'

export function usePessoaCompleta(pessoaId: string | undefined) {
  return useQuery({
    queryKey: ['pessoa-completa', pessoaId],
    queryFn: () => fetchPessoaCompleta(pessoaId as string),
    enabled: Boolean(pessoaId),
  })
}
