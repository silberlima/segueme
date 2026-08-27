import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { MeusDadosPayload } from '../domain/pessoa.types'
import { fetchMeuHistorico, fetchMeusDados, updateMeusDados } from '../services/pessoa.service'

/** Os dados da pessoa que o usuário logado é (perfil Seguidor). */
export function useMeusDados() {
  return useQuery({ queryKey: ['meus-dados'], queryFn: fetchMeusDados })
}

export function useMeuHistorico() {
  return useQuery({ queryKey: ['meus-dados', 'historico'], queryFn: fetchMeuHistorico })
}

export function useAtualizarMeusDados() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: MeusDadosPayload) => updateMeusDados(payload),
    onSuccess: (pessoa) => {
      queryClient.setQueryData(['meus-dados'], pessoa)
      // A alteração aparece também na Lista de Pessoas de quem administra o cadastro.
      queryClient.invalidateQueries({ queryKey: ['pessoas'] })
    },
  })
}
