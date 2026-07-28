import { useMutation, useQueryClient } from '@tanstack/react-query'
import { enviarCandidatos } from '../services/setorial.service'

export function useEnviarCandidatos() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ emprestimoId, pessoaIds }: { emprestimoId: string; pessoaIds: string[] }) =>
      enviarCandidatos(emprestimoId, pessoaIds),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['emprestimos'] })
      queryClient.invalidateQueries({ queryKey: ['candidatos-emprestimo', variables.emprestimoId] })
    },
  })
}
