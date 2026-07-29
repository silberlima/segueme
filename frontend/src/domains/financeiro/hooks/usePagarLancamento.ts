import { useMutation, useQueryClient } from '@tanstack/react-query'
import { pagarLancamento } from '../services/financeiro.service'

export function usePagarLancamento() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: pagarLancamento,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['lancamentos-financeiros'] })
      queryClient.invalidateQueries({ queryKey: ['financeiro', 'resumo'] })
    },
  })
}
