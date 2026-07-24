import { useMutation, useQueryClient } from '@tanstack/react-query'
import { criarPreInscricao } from '../services/inscricao.service'

export function useCriarPreInscricao() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: criarPreInscricao,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pre-inscricoes'] })
      queryClient.invalidateQueries({ queryKey: ['cadastro-reserva'] })
      queryClient.invalidateQueries({ queryKey: ['funil'] })
    },
  })
}
