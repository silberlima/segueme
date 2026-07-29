import { useMutation, useQueryClient } from '@tanstack/react-query'
import { registrarPresencas } from '../services/posencontro.service'

/** Registrar presença muda a frequência e, com ela, o ranking de prioridade (RN-POS-02). */
export function useRegistrarPresencas() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: registrarPresencas,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['presencas'] })
      queryClient.invalidateQueries({ queryKey: ['pos-encontro'] })
    },
  })
}
