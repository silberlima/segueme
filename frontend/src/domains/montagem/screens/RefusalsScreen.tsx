import { Link, useParams } from 'react-router-dom'
import { Badge, ListState } from '../../../shared/ui'
import { PAPEL_INTEGRANTE_LABEL } from '../domain/montagem.types'
import { useConvites } from '../hooks/useConvites'

function formatarData(dataIso: string): string {
  return new Date(dataIso).toLocaleString('pt-BR')
}

/**
 * Recusas (`screen_recusas`): listagem somente-leitura dos convites recusados, com a
 * justificativa obrigatória de cada recusa visível (RF-037, RN-CONVITE-02).
 */
export function RefusalsScreen() {
  const { id: encontroId } = useParams<{ id: string }>()
  const { data: convites, isLoading, isError, refetch } = useConvites(encontroId, { status: 'RECUSADO' })

  return (
    <div className="p-6">
      <Link to={`/encontros/${encontroId}/convites`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Convites
      </Link>

      <h1 className="mb-6 text-xl font-semibold text-slate-900">Recusas</h1>

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={convites?.length === 0}
        onRetry={() => refetch()}
        skeletonRows={1}
        skeletonHeight="120px"
        errorMessage="Não foi possível carregar as recusas."
        emptyMessage="Nenhum convite recusado neste encontro."
      >
        <div className="flex flex-col gap-2">
          {(convites ?? []).map((convite) => (
            <div key={convite.id} className="rounded-lg border border-slate-200 bg-white p-4 text-sm">
              <div className="flex items-center justify-between">
                <p className="font-medium text-slate-900">{convite.pessoaNome}</p>
                <Badge tone="danger">Recusado</Badge>
              </div>
              <p className="text-xs text-slate-500">
                {convite.equipeNome} · {PAPEL_INTEGRANTE_LABEL[convite.papel]}
                {convite.dataResposta && ` · ${formatarData(convite.dataResposta)}`}
              </p>
              {convite.justificativaRecusa && (
                <p className="mt-2 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-800">{convite.justificativaRecusa}</p>
              )}
            </div>
          ))}
        </div>
      </ListState>
    </div>
  )
}
