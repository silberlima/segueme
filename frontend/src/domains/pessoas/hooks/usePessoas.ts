import { useQuery } from '@tanstack/react-query'
import { fetchPessoas } from '../services/pessoa.service'
import type { FiltrosPessoas } from '../services/pessoa.service'

export function usePessoas(filtros: FiltrosPessoas = {}, page = 0, size = 20) {
  return useQuery({
    queryKey: ['pessoas', filtros, page, size],
    queryFn: () => fetchPessoas(filtros, page, size),
  })
}
