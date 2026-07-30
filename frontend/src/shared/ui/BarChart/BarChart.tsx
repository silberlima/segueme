import type { BarChartProps } from './BarChart.types'

/**
 * Gráfico de barras simples dos painéis (`dashboards-por-perfil.md` 2). Sem biblioteca de gráficos
 * no projeto — barras com largura relativa ao maior valor, mesmo padrão de `RevenueExpenseChart`.
 */
export function BarChart({ titulo, itens, formatarValor, mensagemVazio = 'Sem dados no período.' }: BarChartProps) {
  const maior = Math.max(...itens.map((item) => item.valor), 1)

  return (
    <div className="rounded-xl border border-slate-200 bg-white p-4">
      <p className="mb-4 text-sm font-semibold text-slate-900">{titulo}</p>

      {itens.length === 0 && <p className="text-sm text-slate-500">{mensagemVazio}</p>}

      <div className="flex flex-col gap-3">
        {itens.map((item) => (
          <div key={item.rotulo}>
            <div className="mb-1 flex items-center justify-between text-sm">
              <span className="text-slate-600">{item.rotulo}</span>
              <span className="font-medium text-slate-900">
                {formatarValor ? formatarValor(item.valor) : item.valor}
              </span>
            </div>
            <div className="h-2.5 w-full overflow-hidden rounded-full bg-slate-100">
              <div
                className="h-full rounded-full bg-violet-500 transition-all"
                style={{ width: `${Math.round((item.valor / maior) * 100)}%` }}
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
