import { useQuery } from '@tanstack/react-query'
import { fetchEmprestimos } from '../services/setorial.service'
import type { StatusEmprestimo } from '../domain/setorial.types'

export function useEmprestimos(filtros?: { encontroId?: string; equipeId?: string; status?: StatusEmprestimo }) {
  return useQuery({
    queryKey: ['emprestimos', filtros],
    queryFn: () => fetchEmprestimos(filtros),
  })
}
