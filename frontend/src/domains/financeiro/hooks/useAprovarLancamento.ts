import { useMutation, useQueryClient } from '@tanstack/react-query'
import { aprovarLancamento } from '../services/financeiro.service'

export function useAprovarLancamento() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: aprovarLancamento,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['lancamentos-financeiros'] })
    },
  })
}
