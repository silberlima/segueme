import { useMutation, useQueryClient } from '@tanstack/react-query'
import { emitirConvite } from '../services/montagem.service'

export function useEmitirConvite() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: emitirConvite,
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['convites', variables.encontroId] })
      queryClient.invalidateQueries({ queryKey: ['equipes', variables.encontroId] })
      queryClient.invalidateQueries({ queryKey: ['equipe', variables.equipeId, variables.encontroId] })
    },
  })
}
