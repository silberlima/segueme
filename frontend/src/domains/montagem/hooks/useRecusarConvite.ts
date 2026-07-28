import { useMutation, useQueryClient } from '@tanstack/react-query'
import { recusarConvite } from '../services/montagem.service'

export function useRecusarConvite() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ conviteId, justificativa }: { conviteId: string; justificativa: string }) =>
      recusarConvite(conviteId, justificativa),
    onSuccess: (convite) => {
      queryClient.invalidateQueries({ queryKey: ['convites', convite.encontroId] })
      queryClient.invalidateQueries({ queryKey: ['equipe', convite.equipeId, convite.encontroId] })
      queryClient.invalidateQueries({ queryKey: ['equipes', convite.encontroId] })
    },
  })
}
