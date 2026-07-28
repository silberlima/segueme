import { useMutation, useQueryClient } from '@tanstack/react-query'
import { aceitarConvite } from '../services/montagem.service'

export function useAceitarConvite() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: aceitarConvite,
    onSuccess: (convite) => {
      queryClient.invalidateQueries({ queryKey: ['convites', convite.encontroId] })
      queryClient.invalidateQueries({ queryKey: ['equipe', convite.equipeId, convite.encontroId] })
    },
  })
}
