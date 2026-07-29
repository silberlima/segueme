import { useMutation, useQueryClient } from '@tanstack/react-query'
import { designarPalestrante } from '../services/conteudo.service'

export function useDesignarPalestrante(encontroId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ palestraId, palestranteId }: { palestraId: string; palestranteId: string }) =>
      designarPalestrante(encontroId, palestraId, palestranteId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['palestras', encontroId] })
    },
  })
}
