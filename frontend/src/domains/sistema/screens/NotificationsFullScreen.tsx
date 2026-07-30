import { Button, PageHeader, PanelCard } from '../../../shared/ui'
import { NotificationList } from '../components/NotificationList'
import { useNotifications } from '../hooks/useNotifications'

const TAMANHO_DA_PAGINA = 50

/**
 * Notificações (`screen_notificacoes`): a versão em tela cheia do drawer do sino, com uma janela
 * maior de histórico. Reaproveita a mesma lista do drawer para que as duas visões nunca divirjam.
 */
export function NotificationsFullScreen() {
  const { data, isLoading, isError, refetch, naoLidas, marcarComoLidas, isMarcandoLidas } =
    useNotifications(TAMANHO_DA_PAGINA)

  return (
    <div className="p-6">
      <PageHeader
        titulo="Notificações"
        subtitulo={naoLidas > 0 ? `${naoLidas} não lida(s)` : 'Tudo em dia'}
        acoes={
          naoLidas > 0 && (
            <Button variant="secondary" disabled={isMarcandoLidas} onClick={() => marcarComoLidas()}>
              Marcar todas como lidas
            </Button>
          )
        }
      />

      <PanelCard titulo="Recebidas">
        <NotificationList
          notificacoes={data?.content ?? []}
          isLoading={isLoading}
          isError={isError}
          onRetry={() => refetch()}
          mensagemVazio="Nenhuma notificação até agora."
        />
      </PanelCard>
    </div>
  )
}
