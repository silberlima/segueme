import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  fetchNotificacoes,
  fetchNotificacoesNaoLidas,
  marcarNotificacoesComoLidas,
} from '../services/notificacao.service'

const INTERVALO_DO_BADGE_MS = 30_000

/**
 * Central de notificações, compartilhada pelo drawer do sino e pela tela cheia.
 *
 * <p>O total de não lidas (RN-SISTEMA-02) vem de uma contagem própria, não do tamanho da página
 * carregada — o badge precisa estar certo mesmo com mais notificações do que cabem numa página.
 *
 * <p>Nada aqui usa o cache padrão de 1 minuto: notificação nasce no servidor, por evento de outro
 * fluxo (uma recusa de convite, um relatório que ficou pronto), e não por uma ação do usuário que
 * o frontend pudesse invalidar. A lista é sempre reconsultada ao abrir e o contador do sino se
 * atualiza sozinho, senão o badge fica mentindo até a próxima navegação.
 */
export function useNotifications(size = 20) {
  const queryClient = useQueryClient()

  const query = useQuery({
    queryKey: ['notificacoes', 'lista', size],
    queryFn: () => fetchNotificacoes(0, size),
    staleTime: 0,
  })

  const naoLidasQuery = useQuery({
    queryKey: ['notificacoes', 'nao-lidas'],
    queryFn: fetchNotificacoesNaoLidas,
    staleTime: 0,
    refetchInterval: INTERVALO_DO_BADGE_MS,
  })

  const marcarLidasMutation = useMutation({
    mutationFn: marcarNotificacoesComoLidas,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notificacoes'] }),
  })

  return {
    ...query,
    naoLidas: naoLidasQuery.data ?? 0,
    marcarComoLidas: marcarLidasMutation.mutate,
    isMarcandoLidas: marcarLidasMutation.isPending,
  }
}
