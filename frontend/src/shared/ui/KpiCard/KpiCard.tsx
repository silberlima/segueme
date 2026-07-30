import type { KpiCardProps, KpiTone } from './KpiCard.types'

const toneClasses: Record<KpiTone, string> = {
  neutro: 'text-slate-900',
  positivo: 'text-emerald-600',
  atencao: 'text-amber-600',
  critico: 'text-rose-600',
}

/**
 * Cartão de indicador-chave da faixa de 4 KPIs comum a todos os painéis
 * (`dashboards-por-perfil.md` 2): rótulo, valor numérico grande e nota contextual.
 */
export function KpiCard({ rotulo, valor, nota, tone = 'neutro' }: KpiCardProps) {
  // `section` nomeada pelo rótulo: dá ao cartão um papel acessível próprio, e é por ele que os
  // testes e leitores de tela encontram o indicador sem depender da posição na faixa.
  return (
    <section aria-label={rotulo} className="rounded-xl border border-slate-200 bg-white p-4">
      <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">{rotulo}</p>
      <p className={`mt-1.5 text-2xl font-semibold ${toneClasses[tone]}`}>{valor}</p>
      {nota && <p className="mt-1 text-xs text-slate-500">{nota}</p>}
    </section>
  )
}
