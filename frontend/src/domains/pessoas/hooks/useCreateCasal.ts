import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createCasal } from '../services/pessoa.service'

export function useCreateCasal() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: createCasal,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['casais'] })
    },
  })
}
