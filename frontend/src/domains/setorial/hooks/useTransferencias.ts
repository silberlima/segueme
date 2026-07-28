import { useQuery } from '@tanstack/react-query'
import { fetchTransferencias } from '../services/setorial.service'
import type { StatusTransferencia } from '../domain/setorial.types'

export function useTransferencias(filtros?: { status?: StatusTransferencia; pessoaId?: string }) {
  return useQuery({
    queryKey: ['transferencias', filtros],
    queryFn: () => fetchTransferencias(filtros),
  })
}
