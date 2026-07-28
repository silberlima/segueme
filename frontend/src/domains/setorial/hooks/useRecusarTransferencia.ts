import { useMutation, useQueryClient } from '@tanstack/react-query'
import { recusarTransferencia } from '../services/setorial.service'

export function useRecusarTransferencia() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ transferenciaId, justificativa }: { transferenciaId: string; justificativa: string }) =>
      recusarTransferencia(transferenciaId, justificativa),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['transferencias'] })
      queryClient.invalidateQueries({ queryKey: ['solicitacoes'] })
    },
  })
}
