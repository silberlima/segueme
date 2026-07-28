import { useMutation, useQueryClient } from '@tanstack/react-query'
import { reenviarConvite } from '../services/montagem.service'

export function useReenviarConvite() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: reenviarConvite,
    onSuccess: (convite) => {
      queryClient.invalidateQueries({ queryKey: ['convites', convite.encontroId] })
    },
  })
}
