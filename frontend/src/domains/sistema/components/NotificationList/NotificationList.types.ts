import type { Notificacao } from '../../domain/notificacao.types'

export interface NotificationListProps {
  notificacoes: Notificacao[]
  isLoading: boolean
  isError?: boolean
  onRetry?: () => void
  /** Mensagem exibida quando não há notificação — o drawer e a tela cheia dizem coisas diferentes. */
  mensagemVazio: string
}
