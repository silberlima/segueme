import { useQuery } from '@tanstack/react-query'
import { fetchCandidatosEmprestimo } from '../services/setorial.service'

export function useCandidatosEmprestimo(
  emprestimoId: string | undefined,
  filtros?: { paroquiaId?: string; competenciaId?: string },
) {
  return useQuery({
    queryKey: ['candidatos-emprestimo', emprestimoId, filtros],
    queryFn: () => fetchCandidatosEmprestimo(emprestimoId as string, filtros),
    enabled: Boolean(emprestimoId),
  })
}
