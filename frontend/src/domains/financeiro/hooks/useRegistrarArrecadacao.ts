import { useMutation, useQueryClient } from '@tanstack/react-query'
import { registrarArrecadacao } from '../services/financeiro.service'

export function useRegistrarArrecadacao() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ campanhaId, valor }: { campanhaId: string; valor: number }) => registrarArrecadacao(campanhaId, valor),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['campanhas'] })
    },
  })
}
