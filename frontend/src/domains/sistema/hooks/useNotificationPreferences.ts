import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  fetchPreferenciasDeNotificacao,
  salvarPreferenciasDeNotificacao,
} from '../services/notificacao.service'

/**
 * Preferências de notificação do próprio usuário (RF-057). Não há caminho para as de outra pessoa
 * enquanto a pendência #34 não for decidida — o backend usa sempre o `sub` do token.
 */
export function useNotificationPreferences() {
  const queryClient = useQueryClient()

  const query = useQuery({
    queryKey: ['notificacoes', 'preferencias'],
    queryFn: fetchPreferenciasDeNotificacao,
  })

  const salvarMutation = useMutation({
    mutationFn: salvarPreferenciasDeNotificacao,
    onSuccess: (preferencias) => {
      queryClient.setQueryData(['notificacoes', 'preferencias'], preferencias)
      queryClient.invalidateQueries({ queryKey: ['notificacoes'] })
    },
  })

  return {
    ...query,
    salvar: salvarMutation.mutate,
    isSalvando: salvarMutation.isPending,
    salvou: salvarMutation.isSuccess,
  }
}
