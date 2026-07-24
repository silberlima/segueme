import { useMutation, useQueryClient } from '@tanstack/react-query'
import { marcarEmAnalise } from '../services/inscricao.service'

export function useMarcarEmAnalise() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: marcarEmAnalise,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pre-inscricoes'] })
    },
  })
}
