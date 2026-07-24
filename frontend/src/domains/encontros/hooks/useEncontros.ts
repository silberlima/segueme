import { useQuery } from '@tanstack/react-query'
import { fetchEncontros } from '../services/encontro.service'
import type { FiltrosEncontros } from '../services/encontro.service'

export function useEncontros(filtros: FiltrosEncontros = {}, page = 0, size = 20) {
  return useQuery({
    queryKey: ['encontros', filtros, page, size],
    queryFn: () => fetchEncontros(filtros, page, size),
  })
}
