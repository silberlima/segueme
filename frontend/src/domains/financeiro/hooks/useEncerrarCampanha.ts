import { useMutation, useQueryClient } from '@tanstack/react-query'
import { encerrarCampanha } from '../services/financeiro.service'

export function useEncerrarCampanha() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: encerrarCampanha,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['campanhas'] })
    },
  })
}
