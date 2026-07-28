import { useMutation, useQueryClient } from '@tanstack/react-query'
import { efetivarEmprestimo } from '../services/setorial.service'

export function useEfetivarEmprestimo() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: efetivarEmprestimo,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['emprestimos'] })
    },
  })
}
