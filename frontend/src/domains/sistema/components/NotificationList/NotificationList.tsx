import { Badge, Skeleton } from '../../../../shared/ui'
import { LABEL_POR_TIPO, TONE_POR_TIPO, tempoRelativo } from '../../domain/notificacao.entity'
import type { NotificationListProps } from './NotificationList.types'

/**
 * Lista de notificações (RN-SISTEMA-01): ícone por tipo, título, texto, tempo relativo e o
 * indicador de não lida. É a mesma lista no drawer do sino e na tela cheia — as duas visões do
 * mesmo conteúdo não podem divergir.
 */
export function NotificationList({ notificacoes, isLoading, mensagemVazio }: NotificationListProps) {
  if (isLoading) {
    return (
      <div className="flex flex-col gap-3">
        <Skeleton variant="rect" height="4.5rem" />
        <Skeleton variant="rect" height="4.5rem" />
        <Skeleton variant="rect" height="4.5rem" />
      </div>
    )
  }

  if (notificacoes.length === 0) {
    return <p className="text-sm text-slate-500">{mensagemVazio}</p>
  }

  return (
    <ul className="flex flex-col gap-3">
      {notificacoes.map((notificacao) => (
        <li
          key={notificacao.id}
          className={`rounded-lg border p-3 ${
            notificacao.lida ? 'border-slate-100 bg-white' : 'border-violet-100 bg-violet-50/40'
          }`}
        >
          <div className="mb-1 flex items-center justify-between gap-2">
            <Badge tone={TONE_POR_TIPO[notificacao.tipo]}>{LABEL_POR_TIPO[notificacao.tipo]}</Badge>
            <div className="flex items-center gap-2">
              <span className="text-xs text-slate-400">{tempoRelativo(notificacao.createdAt)}</span>
              {!notificacao.lida && (
                <span aria-label="Não lida" className="h-2 w-2 rounded-full bg-violet-600" />
              )}
            </div>
          </div>
          <p className="text-sm font-medium text-slate-900">{notificacao.titulo}</p>
          <p className="text-xs text-slate-500">{notificacao.mensagem}</p>
        </li>
      ))}
    </ul>
  )
}
