import { useQuery } from '@tanstack/react-query'
import { fetchPalestras } from '../services/conteudo.service'

export function usePalestras(encontroId: string) {
  return useQuery({
    queryKey: ['palestras', encontroId],
    queryFn: () => fetchPalestras(encontroId),
    enabled: Boolean(encontroId),
  })
}
