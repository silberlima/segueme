import { useMutation, useQueryClient } from '@tanstack/react-query'
import { cadastrarPalestrante } from '../services/conteudo.service'

export function useCadastrarPalestrante() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: cadastrarPalestrante,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['palestrantes'] })
    },
  })
}
