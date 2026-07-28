import { useMutation, useQueryClient } from '@tanstack/react-query'
import { aprovarTransferencia } from '../services/setorial.service'

export function useAprovarTransferencia() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: aprovarTransferencia,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['transferencias'] })
      queryClient.invalidateQueries({ queryKey: ['solicitacoes'] })
    },
  })
}
