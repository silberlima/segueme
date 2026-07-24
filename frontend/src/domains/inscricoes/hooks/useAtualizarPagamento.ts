import { useMutation, useQueryClient } from '@tanstack/react-query'
import { atualizarPagamento } from '../services/inscricao.service'
import type { StatusPagamento } from '../domain/inscricao.types'

export function useAtualizarPagamento() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ fichaInscricaoId, statusPagamento }: { fichaInscricaoId: string; statusPagamento: StatusPagamento }) =>
      atualizarPagamento(fichaInscricaoId, statusPagamento),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['participantes'] })
    },
  })
}
