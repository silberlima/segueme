import type { OcupacaoVagasBarProps } from './OcupacaoVagasBar.types'

/** Indicador de ocupação de vagas do Encontro ("84/90" + "8 em reserva", `inscricoes.md` 2.1). */
export function OcupacaoVagasBar({ ocupadas, vagas, emReserva }: OcupacaoVagasBarProps) {
  const percentual = vagas > 0 ? Math.min(100, Math.round((ocupadas / vagas) * 100)) : 0

  return (
    <div className="rounded-xl border border-slate-200 bg-white p-4">
      <div className="mb-2 flex items-center justify-between text-sm">
        <span className="font-medium text-slate-900">
          {ocupadas}/{vagas} vagas ocupadas
        </span>
        {emReserva > 0 && <span className="text-amber-600">{emReserva} em reserva</span>}
      </div>
      <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100">
        <div
          className={`h-full rounded-full transition-all ${percentual >= 100 ? 'bg-amber-500' : 'bg-violet-600'}`}
          style={{ width: `${percentual}%` }}
        />
      </div>
    </div>
  )
}
