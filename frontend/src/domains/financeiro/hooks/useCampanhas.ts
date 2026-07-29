import { useQuery } from '@tanstack/react-query'
import { fetchCampanhas } from '../services/financeiro.service'
import type { StatusCampanha } from '../domain/financeiro.types'

export function useCampanhas(encontroId: string, filtros?: { status?: StatusCampanha }) {
  return useQuery({
    queryKey: ['campanhas', encontroId, filtros],
    queryFn: () => fetchCampanhas({ encontroId, ...filtros }),
    enabled: Boolean(encontroId),
  })
}
