import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createEncontro } from '../services/encontro.service'

export function useCreateEncontro() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: createEncontro,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['encontros'] })
    },
  })
}
