import { useQuery } from '@tanstack/react-query'
import { fetchPreInscricoes } from '../services/inscricao.service'
import type { FiltrosPreInscricoes } from '../services/inscricao.service'

export function usePreInscricoes(filtros: FiltrosPreInscricoes = {}, page = 0, size = 20) {
  return useQuery({
    queryKey: ['pre-inscricoes', filtros, page, size],
    queryFn: () => fetchPreInscricoes(filtros, page, size),
    enabled: Boolean(filtros.encontroId),
  })
}
