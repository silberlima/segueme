import { useMutation, useQueryClient } from '@tanstack/react-query'
import { registrarLancamento } from '../services/financeiro.service'

export function useRegistrarLancamento() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: registrarLancamento,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['lancamentos-financeiros'] })
      queryClient.invalidateQueries({ queryKey: ['financeiro', 'resumo'] })
    },
  })
}
