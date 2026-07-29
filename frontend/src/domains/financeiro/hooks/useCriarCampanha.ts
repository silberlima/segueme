import { useMutation, useQueryClient } from '@tanstack/react-query'
import { criarCampanha } from '../services/financeiro.service'

export function useCriarCampanha() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: criarCampanha,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['campanhas'] })
      queryClient.invalidateQueries({ queryKey: ['financeiro', 'resumo'] })
    },
  })
}
