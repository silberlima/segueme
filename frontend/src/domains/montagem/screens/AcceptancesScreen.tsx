import { Link, useParams } from 'react-router-dom'
import { Badge, ListState } from '../../../shared/ui'
import { PAPEL_INTEGRANTE_LABEL } from '../domain/montagem.types'
import { useConvites } from '../hooks/useConvites'

function formatarData(dataIso: string): string {
  return new Date(dataIso).toLocaleString('pt-BR')
}

/** Aceites (`screen_aceites`): listagem somente-leitura dos convites já aceitos (RF-036). */
export function AcceptancesScreen() {
  const { id: encontroId } = useParams<{ id: string }>()
  const { data: convites, isLoading, isError, refetch } = useConvites(encontroId, { status: 'ACEITO' })

  return (
    <div className="p-6">
      <Link to={`/encontros/${encontroId}/convites`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Convites
      </Link>

      <h1 className="mb-6 text-xl font-semibold text-slate-900">Aceites</h1>

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={convites?.length === 0}
        onRetry={() => refetch()}
        skeletonRows={1}
        skeletonHeight="120px"
        errorMessage="Não foi possível carregar os aceites."
        emptyMessage="Nenhum convite aceito neste encontro ainda."
      >
        <div className="flex flex-col gap-2">
          {(convites ?? []).map((convite) => (
            <div key={convite.id} className="rounded-lg border border-slate-200 bg-white p-4 text-sm">
              <div className="flex items-center justify-between">
                <p className="font-medium text-slate-900">{convite.pessoaNome}</p>
                <Badge tone="success">Aceito</Badge>
              </div>
              <p className="text-xs text-slate-500">
                {convite.equipeNome} · {PAPEL_INTEGRANTE_LABEL[convite.papel]}
                {convite.dataResposta && ` · ${formatarData(convite.dataResposta)}`}
              </p>
            </div>
          ))}
        </div>
      </ListState>
    </div>
  )
}
