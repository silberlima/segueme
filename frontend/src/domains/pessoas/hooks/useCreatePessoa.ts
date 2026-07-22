import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createPessoa } from '../services/pessoa.service'

export function useCreatePessoa() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: createPessoa,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pessoas'] })
    },
  })
}
