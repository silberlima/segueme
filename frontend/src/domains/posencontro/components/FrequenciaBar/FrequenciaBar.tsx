import type { FaixaPrioridade } from '../../domain/posencontro.types'
import type { FrequenciaBarProps } from './FrequenciaBar.types'

const corPorFaixa: Record<FaixaPrioridade, string> = {
  ALTA: 'bg-emerald-500',
  MEDIA: 'bg-amber-500',
  BAIXA: 'bg-rose-500',
}

/**
 * Barra de frequência colorida pela faixa de prioridade (RN-POS-03). Sem biblioteca de gráficos no
 * projeto — mesmo padrão sancionado em `CampanhaProgressBar`.
 */
export function FrequenciaBar({ percentual, faixa }: FrequenciaBarProps) {
  return (
    <div className="flex items-center gap-2">
      <div className="h-2 w-24 overflow-hidden rounded-full bg-slate-100">
        <div className={`h-full rounded-full transition-all ${corPorFaixa[faixa]}`} style={{ width: `${percentual}%` }} />
      </div>
      <span className="text-sm text-slate-600">{percentual}%</span>
    </div>
  )
}
