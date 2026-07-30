import type { KpiRowProps } from './KpiRow.types'

/**
 * Faixa de indicadores-chave dos painéis (`dashboards-por-perfil.md` 2): sempre a mesma grade nos
 * nove painéis, para que a leitura não mude de forma ao trocar de perfil.
 */
export function KpiRow({ children }: KpiRowProps) {
  return <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">{children}</div>
}
