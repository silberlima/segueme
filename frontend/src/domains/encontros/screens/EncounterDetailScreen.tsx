import { Link, useParams } from 'react-router-dom'
import { Badge, Skeleton } from '../../../shared/ui'
import { STATUS_ENCONTRO_LABEL } from '../domain/encontro.types'
import { useEncontro } from '../hooks/useEncontro'

function Campo({ label, valor }: { label: string; valor: string }) {
  return (
    <div>
      <p className="text-xs font-medium text-slate-500">{label}</p>
      <p className="text-sm text-slate-900">{valor}</p>
    </div>
  )
}

export function EncounterDetailScreen() {
  const { id } = useParams<{ id: string }>()
  const { data: encontro, isLoading, isError } = useEncontro(id)

  return (
    <div className="p-6">
      <Link to="/encontros" className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Lista de Encontros
      </Link>

      {isLoading && (
        <div className="flex flex-col gap-2">
          <Skeleton variant="rect" height="160px" />
        </div>
      )}

      {isError && (
        <p className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          Não foi possível carregar os detalhes deste encontro.
        </p>
      )}

      {encontro && (
        <div className="rounded-xl border border-slate-200 bg-white p-6">
          <div className="mb-6 flex items-center justify-between">
            <div>
              <h1 className="text-xl font-semibold text-slate-900">
                #{encontro.numero} · {encontro.padroeiro}
              </h1>
              <p className="text-sm text-slate-500">{encontro.paroquiaNome}</p>
            </div>
            <Badge tone={encontro.status === 'ATIVO' ? 'success' : 'neutral'}>
              {STATUS_ENCONTRO_LABEL[encontro.status]}
            </Badge>
          </div>

          <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
            <Campo label="Diretor espiritual" valor={encontro.diretorEspiritualNome} />
            <Campo label="Local" valor={encontro.local} />
            <Campo label="Vagas" valor={String(encontro.vagas)} />
            <Campo label="Data de início" valor={encontro.dataInicio} />
            <Campo label="Data de término" valor={encontro.dataTermino} />
          </div>

          <div>
            <p className="mb-2 text-xs font-medium text-slate-500">Equipes</p>
            <div className="flex flex-wrap gap-3">
              <Link
                to={`/encontros/${encontro.id}/dirigente`}
                className="rounded-lg border border-slate-200 px-4 py-3 text-sm font-medium text-slate-900 hover:border-violet-300 hover:text-violet-700"
              >
                Equipe Dirigente ({encontro.equipeDirigente.length} membros) →
              </Link>
              <Link
                to={`/encontros/${encontro.id}/comando`}
                className="rounded-lg border border-slate-200 px-4 py-3 text-sm font-medium text-slate-900 hover:border-violet-300 hover:text-violet-700"
              >
                Equipe Comando ({encontro.equipeComando.coordenadorGeralNome}) →
              </Link>
              <Link
                to={`/encontros/${encontro.id}/equipes`}
                className="rounded-lg border border-slate-200 px-4 py-3 text-sm font-medium text-slate-900 hover:border-violet-300 hover:text-violet-700"
              >
                Equipes de Serviço →
              </Link>
              <Link
                to={`/encontros/${encontro.id}/convites`}
                className="rounded-lg border border-slate-200 px-4 py-3 text-sm font-medium text-slate-900 hover:border-violet-300 hover:text-violet-700"
              >
                Convites →
              </Link>
              <Link
                to={`/encontros/${encontro.id}/aceites`}
                className="rounded-lg border border-slate-200 px-4 py-3 text-sm font-medium text-slate-900 hover:border-violet-300 hover:text-violet-700"
              >
                Aceites →
              </Link>
              <Link
                to={`/encontros/${encontro.id}/recusas`}
                className="rounded-lg border border-slate-200 px-4 py-3 text-sm font-medium text-slate-900 hover:border-violet-300 hover:text-violet-700"
              >
                Recusas →
              </Link>
            </div>
          </div>

          <div className="mt-6">
            <p className="mb-2 text-xs font-medium text-slate-500">Inscrições</p>
            <div className="flex flex-wrap gap-3">
              <Link
                to={`/encontros/${encontro.id}/inscricoes`}
                className="rounded-lg border border-slate-200 px-4 py-3 text-sm font-medium text-slate-900 hover:border-violet-300 hover:text-violet-700"
              >
                Pré-inscrições →
              </Link>
              <Link
                to={`/encontros/${encontro.id}/reserva`}
                className="rounded-lg border border-slate-200 px-4 py-3 text-sm font-medium text-slate-900 hover:border-violet-300 hover:text-violet-700"
              >
                Lista de Espera →
              </Link>
              <Link
                to={`/encontros/${encontro.id}/participantes`}
                className="rounded-lg border border-slate-200 px-4 py-3 text-sm font-medium text-slate-900 hover:border-violet-300 hover:text-violet-700"
              >
                Participantes →
              </Link>
            </div>
          </div>

          <div className="mt-6">
            <p className="mb-2 text-xs font-medium text-slate-500">Financeiro</p>
            <div className="flex flex-wrap gap-3">
              <Link
                to={`/encontros/${encontro.id}/financeiro`}
                className="rounded-lg border border-slate-200 px-4 py-3 text-sm font-medium text-slate-900 hover:border-violet-300 hover:text-violet-700"
              >
                Financeiro →
              </Link>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
