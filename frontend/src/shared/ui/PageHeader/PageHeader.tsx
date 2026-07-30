import type { PageHeaderProps } from './PageHeader.types'

/**
 * Cabeçalho comum dos painéis (`dashboards-por-perfil.md` 2): título, subtítulo de escopo e as
 * ações principais no topo direito.
 */
export function PageHeader({ titulo, subtitulo, acoes }: PageHeaderProps) {
  return (
    <header className="mb-6 flex flex-wrap items-start justify-between gap-3">
      <div>
        <h1 className="text-xl font-semibold text-slate-900">{titulo}</h1>
        {subtitulo && <p className="mt-1 text-sm text-slate-500">{subtitulo}</p>}
      </div>
      {acoes && <div className="flex flex-wrap items-center gap-2">{acoes}</div>}
    </header>
  )
}
