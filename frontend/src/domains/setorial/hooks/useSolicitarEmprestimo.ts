import { useMutation, useQueryClient } from '@tanstack/react-query'
import { solicitarEmprestimo } from '../services/setorial.service'

export function useSolicitarEmprestimo() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: solicitarEmprestimo,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['emprestimos'] })
      queryClient.invalidateQueries({ queryKey: ['solicitacoes'] })
    },
  })
}
