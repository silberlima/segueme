import { useMutation, useQueryClient } from '@tanstack/react-query'
import { recusarEmprestimo } from '../services/setorial.service'

export function useRecusarEmprestimo() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ emprestimoId, justificativa }: { emprestimoId: string; justificativa: string }) =>
      recusarEmprestimo(emprestimoId, justificativa),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['emprestimos'] })
      queryClient.invalidateQueries({ queryKey: ['solicitacoes'] })
    },
  })
}
