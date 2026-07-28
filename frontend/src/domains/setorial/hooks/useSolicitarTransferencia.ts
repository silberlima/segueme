import { useMutation, useQueryClient } from '@tanstack/react-query'
import { solicitarTransferencia } from '../services/setorial.service'

export function useSolicitarTransferencia() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: solicitarTransferencia,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['transferencias'] })
      queryClient.invalidateQueries({ queryKey: ['solicitacoes'] })
    },
  })
}
