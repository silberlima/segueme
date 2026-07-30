import type { ProgressBarProps, ProgressTone } from './ProgressBar.types'

const toneClasses: Record<ProgressTone, string> = {
  violeta: 'bg-violet-500',
  verde: 'bg-emerald-500',
  ambar: 'bg-amber-500',
}

/** Barra de progresso dos painéis (equipes montadas, inscrições confirmadas, meta de campanha). */
export function ProgressBar({ rotulo, detalhe, percentual, tone = 'violeta' }: ProgressBarProps) {
  return (
    <div>
      <div className="mb-1 flex items-center justify-between text-sm">
        <span className="text-slate-600">{rotulo}</span>
        <span className="font-medium text-slate-900">{detalhe}</span>
      </div>
      <div className="h-2.5 w-full overflow-hidden rounded-full bg-slate-100">
        <div className={`h-full rounded-full transition-all ${toneClasses[tone]}`} style={{ width: `${percentual}%` }} />
      </div>
    </div>
  )
}
