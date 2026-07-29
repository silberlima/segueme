import { useMutation, useQueryClient } from '@tanstack/react-query'
import { agendarEncontroPos } from '../services/posencontro.service'

export function useAgendarEncontroPos() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: agendarEncontroPos,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['encontros-pos'] })
      queryClient.invalidateQueries({ queryKey: ['pos-encontro'] })
    },
  })
}
