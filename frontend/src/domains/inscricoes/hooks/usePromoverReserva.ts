import { useMutation, useQueryClient } from '@tanstack/react-query'
import { promoverReserva } from '../services/inscricao.service'

interface PromoverReservaInput {
  cadastroReservaId: string
  encontroDestinoId: string
}

export function usePromoverReserva() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ cadastroReservaId, encontroDestinoId }: PromoverReservaInput) =>
      promoverReserva(cadastroReservaId, encontroDestinoId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cadastro-reserva'] })
      queryClient.invalidateQueries({ queryKey: ['pre-inscricoes'] })
      queryClient.invalidateQueries({ queryKey: ['funil'] })
    },
  })
}
