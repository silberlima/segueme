import { useQuery } from '@tanstack/react-query'
import { fetchLancamentos } from '../services/financeiro.service'
import type { SituacaoDespesa, TipoLancamento } from '../domain/financeiro.types'

export function useLancamentos(encontroId: string, filtros?: { tipo?: TipoLancamento; situacao?: SituacaoDespesa }) {
  return useQuery({
    queryKey: ['lancamentos-financeiros', encontroId, filtros],
    queryFn: () => fetchLancamentos({ encontroId, ...filtros }),
    enabled: Boolean(encontroId),
  })
}
