import { useQuery } from '@tanstack/react-query'
import { fetchPessoasPorCompetencia } from '../services/pessoa.service'

export function usePessoasPorCompetencia(competenciaId: string | undefined, page = 0, size = 20) {
  return useQuery({
    queryKey: ['pessoas-por-competencia', competenciaId, page, size],
    queryFn: () => fetchPessoasPorCompetencia(competenciaId as string, page, size),
    enabled: Boolean(competenciaId),
  })
}
