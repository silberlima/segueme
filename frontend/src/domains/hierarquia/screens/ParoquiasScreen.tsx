import { Badge, Skeleton } from '../../../shared/ui'
import { useParoquias } from '../hooks/useParoquias'

export function ParoquiasScreen() {
  const { data, isLoading, isError } = useParoquias(0, 50)

  return (
    <div className="p-6">
      <h1 className="mb-1 text-xl font-semibold text-slate-900">Paróquias</h1>
      <p className="mb-6 text-sm text-slate-500">Paróquias do seu escopo de acesso.</p>

      {isLoading && (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
          <Skeleton variant="rect" />
          <Skeleton variant="rect" />
          <Skeleton variant="rect" />
        </div>
      )}

      {isError && (
        <p className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          Não foi possível carregar as paróquias. Tente novamente mais tarde.
        </p>
      )}

      {data && data.content.length === 0 && (
        <p className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          Nenhuma paróquia encontrada para o seu escopo de acesso.
        </p>
      )}

      {data && data.content.length > 0 && (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {data.content.map((paroquia) => (
            <div key={paroquia.id} className="rounded-xl border border-slate-200 bg-white p-4">
              <p className="text-sm font-medium text-slate-900">{paroquia.nome}</p>
              <p className="mt-1 text-xs text-slate-500">
                {paroquia.setorNome} · {paroquia.dioceseNome}
              </p>
              <div className="mt-3 flex items-center justify-between">
                <span className="text-xs text-slate-500">{paroquia.numSeguidores} seguidores</span>
                <Badge tone={paroquia.statusEncontroAtual ? 'success' : 'neutral'}>
                  {paroquia.statusEncontroAtual ?? '—'}
                </Badge>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
