import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adicionarIntegrante } from '../services/montagem.service'
import type { PapelIntegrante } from '../domain/montagem.types'

export function useAdicionarIntegrante() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({
      equipeId,
      encontroId,
      pessoaId,
      papel,
    }: {
      equipeId: string
      encontroId: string
      pessoaId: string
      papel: PapelIntegrante
    }) => adicionarIntegrante(equipeId, encontroId, pessoaId, papel),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['equipes', variables.encontroId] })
      queryClient.invalidateQueries({ queryKey: ['equipe', variables.equipeId, variables.encontroId] })
      queryClient.invalidateQueries({ queryKey: ['sugestoes', variables.encontroId, variables.equipeId] })
    },
  })
}
