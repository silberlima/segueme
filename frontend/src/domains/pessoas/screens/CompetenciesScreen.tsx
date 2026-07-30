import { useNavigate } from 'react-router-dom'
import { ListState } from '../../../shared/ui'
import { useCompetencias } from '../hooks/useCompetencias'

export function CompetenciesScreen() {
  const navigate = useNavigate()
  const { data: competencias, isLoading, isError, refetch } = useCompetencias()

  return (
    <div className="p-6">
      <h1 className="mb-1 text-xl font-semibold text-slate-900">Competências</h1>
      <p className="mb-6 text-sm text-slate-500">
        Catálogo nacional de dons/habilidades (RF-013), usado na composição de equipes.
      </p>

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={competencias?.length === 0}
        onRetry={() => refetch()}
        skeletonHeight="80px"
        errorMessage="Não foi possível carregar o catálogo de competências."
        emptyMessage="Nenhuma competência cadastrada."
      >
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {(competencias ?? []).map((competencia) => (
            <button
              key={competencia.id}
              type="button"
              onClick={() => navigate(`/pessoas?competenciaId=${competencia.id}`)}
              className="rounded-xl border border-slate-200 bg-white p-4 text-left transition-colors hover:border-violet-300"
            >
              <p className="text-sm font-medium text-slate-900">{competencia.nome}</p>
              <p className="mt-1 text-xs text-slate-500">{competencia.numPessoas} pessoas</p>
            </button>
          ))}
        </div>
      </ListState>
    </div>
  )
}
