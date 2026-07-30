import { Link, useParams } from 'react-router-dom'
import { ListState } from '../../../shared/ui'
import { useEncontro } from '../hooks/useEncontro'

export function ComandoTeamScreen() {
  const { id } = useParams<{ id: string }>()
  const { data: encontro, isLoading, isError, refetch } = useEncontro(id)

  return (
    <div className="p-6">
      <Link to={`/encontros/${id}`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Detalhes do Encontro
      </Link>

      <ListState
        isLoading={isLoading}
        isError={isError}
        onRetry={() => refetch()}
        skeletonRows={1}
        skeletonHeight="120px"
        errorMessage="Não foi possível carregar a Equipe Comando deste encontro."
      />

      {encontro && (
        <div className="rounded-xl border border-slate-200 bg-white p-6">
          <h1 className="mb-1 text-xl font-semibold text-slate-900">Equipe Comando</h1>
          <p className="mb-4 text-sm text-slate-500">
            #{encontro.numero} · {encontro.padroeiro}
          </p>

          <ul className="flex flex-col gap-2">
            <li className="flex items-center justify-between rounded-lg border border-slate-200 px-3 py-2 text-sm">
              <span className="text-slate-900">{encontro.equipeComando.coordenadorGeralNome}</span>
              <span className="text-xs text-slate-500">Coordenador geral</span>
            </li>
            <li className="flex items-center justify-between rounded-lg border border-slate-200 px-3 py-2 text-sm">
              <span className="text-slate-900">{encontro.equipeComando.casalCoordenadorNome}</span>
              <span className="text-xs text-slate-500">Casal coordenador</span>
            </li>
          </ul>

          <p className="mt-4 text-xs text-slate-500">
            A Comando é responsável por convidar os coordenadores das demais equipes e acompanhar aceites e recusas
            (funcionalidade prevista para uma fase futura).
          </p>
        </div>
      )}
    </div>
  )
}
