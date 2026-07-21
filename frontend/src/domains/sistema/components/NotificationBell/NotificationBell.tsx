import { Badge, Drawer } from '../../../../shared/ui'
import type { BadgeTone } from '../../../../shared/ui'
import { useDisclosure } from '../../../../shared/hooks/useDisclosure'
import { useNotifications } from '../../hooks/useNotifications'
import type { TipoNotificacao } from '../../domain/notificacao.types'
import type { NotificationBellProps } from './NotificationBell.types'

const TONE_POR_TIPO: Record<TipoNotificacao, BadgeTone> = {
  INFORMATIVA: 'info',
  POSITIVA: 'success',
  NEGATIVA: 'danger',
}

export function NotificationBell({ className = '' }: NotificationBellProps) {
  const { isOpen, open, close } = useDisclosure()
  const { data, naoLidas, marcarComoLidas, isLoading } = useNotifications()

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
        {naoLidas > 0 && (
          <button
            type="button"
            onClick={() => marcarComoLidas()}
            className="mb-4 text-sm font-medium text-violet-600 hover:underline"
          >
            Marcar todas como lidas
          </button>
        )}
        {isLoading && <p className="text-sm text-slate-400">Carregando...</p>}
        {!isLoading && (data?.content.length ?? 0) === 0 && (
          <p className="text-sm text-slate-400">Nenhuma notificação.</p>
        )}
        <ul className="flex flex-col gap-3">
          {data?.content.map((notificacao) => (
            <li key={notificacao.id} className="rounded-lg border border-slate-100 p-3">
              <div className="mb-1 flex items-center justify-between">
                <Badge tone={TONE_POR_TIPO[notificacao.tipo]}>{notificacao.tipo}</Badge>
                {!notificacao.lida && <span className="h-2 w-2 rounded-full bg-violet-600" />}
              </div>
              <p className="text-sm font-medium text-slate-900">{notificacao.titulo}</p>
              <p className="text-xs text-slate-500">{notificacao.mensagem}</p>
            </li>
          ))}
        </ul>
      </Drawer>
    </>
  )
}
