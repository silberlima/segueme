import { useQuery } from '@tanstack/react-query'
import { fetchPalestrantes } from '../services/conteudo.service'
import type { TipoPalestrante } from '../domain/conteudo.types'

export function usePalestrantes(filtros?: { tipo?: TipoPalestrante }) {
  return useQuery({
    queryKey: ['palestrantes', filtros],
    queryFn: () => fetchPalestrantes(filtros),
  })
}
