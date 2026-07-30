import { Link } from 'react-router-dom'
import { Drawer } from '../../../../shared/ui'
import { useDisclosure } from '../../../../shared/hooks/useDisclosure'
import { useNotifications } from '../../hooks/useNotifications'
import { NotificationList } from '../NotificationList'
import type { NotificationBellProps } from './NotificationBell.types'

/**
 * Sino do header (RN-SISTEMA-02): badge de não lidas e o drawer com a central. O drawer mostra o
 * recorte recente e leva à tela cheia (`screen_notificacoes`) para o histórico completo.
 */
export function NotificationBell({ className = '' }: NotificationBellProps) {
  const { isOpen, open, close } = useDisclosure()
  const { data, naoLidas, marcarComoLidas, isMarcandoLidas, isLoading } = useNotifications()

  return (
    <>
      <button
        type="button"
        onClick={open}
        aria-label="Notificações"
        className={`relative rounded-full p-2 text-lg hover:bg-slate-100 ${className}`}
      >
        🔔
        {naoLidas > 0 && (
          <span className="absolute -right-0.5 -top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-rose-600 px-1 text-[10px] font-semibold text-white">
            {naoLidas}
          </span>
        )}
      </button>

      <Drawer isOpen={isOpen} onClose={close} title="Notificações">
        <div className="mb-4 flex items-center justify-between gap-3">
          <Link
            to="/notificacoes"
            onClick={close}
            className="text-sm font-medium text-violet-600 hover:underline"
          >
            Ver todas
          </Link>
          {naoLidas > 0 && (
            <button
              type="button"
              disabled={isMarcandoLidas}
              onClick={() => marcarComoLidas()}
              className="text-sm font-medium text-violet-600 hover:underline disabled:opacity-50"
            >
              Marcar todas como lidas
            </button>
          )}
        </div>

        <NotificationList
          notificacoes={data?.content ?? []}
          isLoading={isLoading}
          mensagemVazio="Nenhuma notificação."
        />
      </Drawer>
    </>
  )
}
