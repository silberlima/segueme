import { useMutation, useQueryClient } from '@tanstack/react-query'
import { registrarSubstituicao } from '../services/montagem.service'

export function useRegistrarSubstituicao() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: registrarSubstituicao,
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['substituicoes', variables.encontroId] })
      queryClient.invalidateQueries({ queryKey: ['equipes', variables.encontroId] })
      queryClient.invalidateQueries({ queryKey: ['equipe', variables.equipeId, variables.encontroId] })
    },
  })
}
