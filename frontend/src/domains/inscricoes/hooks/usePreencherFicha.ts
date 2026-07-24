import { useMutation, useQueryClient } from '@tanstack/react-query'
import { preencherFicha } from '../services/inscricao.service'

export function usePreencherFicha() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: preencherFicha,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pre-inscricoes'] })
      queryClient.invalidateQueries({ queryKey: ['participantes'] })
      queryClient.invalidateQueries({ queryKey: ['funil'] })
    },
  })
}
