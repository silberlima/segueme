import { Link } from 'react-router-dom'
import { Badge } from '../../../shared/ui'
import { getCatalogoAgrupado } from '../domain/navegacao.entity'

export function NavigationMapScreen() {
  const grupos = getCatalogoAgrupado()

  return (
    <div className="p-6">
      <h1 className="mb-1 text-xl font-semibold text-slate-900">Mapa de navegação</h1>
      <p className="mb-6 text-sm text-slate-500">
        Catálogo completo das telas do sistema, agrupadas por módulo.
      </p>

      <div className="flex flex-col gap-8">
        {Array.from(grupos.entries()).map(([modulo, itens]) => (
          <section key={modulo}>
            <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-400">{modulo}</h2>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {itens.map((item) => {
                const conteudo = (
                  <div className="flex h-full flex-col justify-between rounded-xl border border-slate-200 bg-white p-4">
                    <p className="text-sm font-medium text-slate-900">{item.titulo}</p>
                    <div className="mt-3">
                      <Badge tone={item.status === 'pronto' ? 'success' : 'neutral'}>
                        {item.status === 'pronto' ? 'Pronto para navegar' : 'Previsto no escopo'}
                      </Badge>
                    </div>
                  </div>
                )
                return item.path ? (
                  <Link key={item.titulo} to={item.path} className="transition-shadow hover:shadow-sm">
                    {conteudo}
                  </Link>
                ) : (
                  <div key={item.titulo} className="opacity-70">
                    {conteudo}
                  </div>
                )
              })}
            </div>
          </section>
        ))}
      </div>
    </div>
  )
}
