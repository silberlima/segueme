import type { CampanhaProgressBarProps } from './CampanhaProgressBar.types'

const formatador = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

/** Progresso de uma Campanha de arrecadação até a meta (`financeiro.md` 2.4). */
export function CampanhaProgressBar({ valorArrecadado, meta }: CampanhaProgressBarProps) {
  const percentual = meta > 0 ? Math.min(100, Math.round((valorArrecadado / meta) * 100)) : 0

  return (
    <div>
      <div className="mb-2 flex items-center justify-between text-sm">
        <span className="font-medium text-slate-900">
          {formatador.format(valorArrecadado)} / {formatador.format(meta)}
        </span>
        <span className="text-slate-500">{percentual}%</span>
      </div>
      <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100">
        <div
          className={`h-full rounded-full transition-all ${percentual >= 100 ? 'bg-emerald-500' : 'bg-violet-600'}`}
          style={{ width: `${percentual}%` }}
        />
      </div>
    </div>
  )
}
