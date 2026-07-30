import type { PanelCardProps } from './PanelCard.types'

/**
 * Cartão de conteúdo dos painéis: título, atalho opcional "Ver todos" e o corpo. Padroniza o
 * enquadramento das listas e destaques dos nove painéis (`dashboards-por-perfil.md` 2).
 */
export function PanelCard({ titulo, acao, children }: PanelCardProps) {
  return (
    <section className="rounded-xl border border-slate-200 bg-white p-4">
      <div className="mb-3 flex items-center justify-between gap-3">
        <h2 className="text-sm font-semibold text-slate-900">{titulo}</h2>
        {acao}
      </div>
      {children}
    </section>
  )
}
