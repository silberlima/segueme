import { useQuery } from '@tanstack/react-query'
import { fetchFrequencia } from '../services/posencontro.service'
import type { FiltroFrequencia } from '../domain/posencontro.types'

export function useFrequencia(filtro: FiltroFrequencia) {
  return useQuery({
    queryKey: ['pos-encontro', 'frequencia', filtro],
    queryFn: () => fetchFrequencia(filtro),
    enabled: Boolean(filtro.paroquiaId),
  })
}
