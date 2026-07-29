import type { RevenueExpenseChartProps } from './RevenueExpenseChart.types'

const formatador = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

/**
 * Gráfico de barras receitas × despesas (`financeiro.md` 2.1). Sem biblioteca de gráficos no
 * projeto — duas barras com largura dinâmica, mesmo padrão sancionado em `OcupacaoVagasBar`.
 */
export function RevenueExpenseChart({ totalReceitas, totalDespesas }: RevenueExpenseChartProps) {
  const maior = Math.max(totalReceitas, totalDespesas, 1)
  const percentualReceitas = Math.round((totalReceitas / maior) * 100)
  const percentualDespesas = Math.round((totalDespesas / maior) * 100)

  return (
    <div className="rounded-xl border border-slate-200 bg-white p-4">
      <p className="mb-4 text-sm font-semibold text-slate-900">Receitas × Despesas</p>
      <div className="flex flex-col gap-3">
        <div>
          <div className="mb-1 flex items-center justify-between text-sm">
            <span className="text-slate-600">Receitas</span>
            <span className="font-medium text-slate-900">{formatador.format(totalReceitas)}</span>
          </div>
          <div className="h-2.5 w-full overflow-hidden rounded-full bg-slate-100">
            <div className="h-full rounded-full bg-emerald-500 transition-all" style={{ width: `${percentualReceitas}%` }} />
          </div>
        </div>
        <div>
          <div className="mb-1 flex items-center justify-between text-sm">
            <span className="text-slate-600">Despesas</span>
            <span className="font-medium text-slate-900">{formatador.format(totalDespesas)}</span>
          </div>
          <div className="h-2.5 w-full overflow-hidden rounded-full bg-slate-100">
            <div className="h-full rounded-full bg-rose-500 transition-all" style={{ width: `${percentualDespesas}%` }} />
          </div>
        </div>
      </div>
    </div>
  )
}
