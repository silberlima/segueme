import { useQuery } from '@tanstack/react-query'
import { fetchEncontroContexto } from '../services/inscricao.service'

export function useEncontroContexto(encontroId: string | undefined) {
  return useQuery({
    queryKey: ['encontro-contexto', encontroId],
    queryFn: () => fetchEncontroContexto(encontroId as string),
    enabled: Boolean(encontroId),
  })
}
