import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { fetchNotificacoes, marcarNotificacoesComoLidas } from '../services/notificacao.service'

export function useNotifications() {
  const queryClient = useQueryClient()

  const query = useQuery({
    queryKey: ['notificacoes'],
    queryFn: () => fetchNotificacoes(0, 20),
  })

  const marcarLidasMutation = useMutation({
    mutationFn: marcarNotificacoesComoLidas,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notificacoes'] }),
  })

  const naoLidas = query.data?.content.filter((notificacao) => !notificacao.lida).length ?? 0

  return {
    ...query,
    naoLidas,
    marcarComoLidas: marcarLidasMutation.mutate,
  }
}
