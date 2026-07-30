import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useProfile } from '../../../app/providers/ProfileProvider'
import { Badge, Button, ListState, Select } from '../../../shared/ui'
import { TIPO_PALESTRANTE_LABEL } from '../domain/conteudo.types'
import type { PalestraDoEncontro } from '../domain/conteudo.types'
import { useDesignarPalestrante } from '../hooks/useDesignarPalestrante'
import { usePalestras } from '../hooks/usePalestras'
import { usePalestrantes } from '../hooks/usePalestrantes'

function LinhaPalestra({
  palestra,
  podeDesignar,
  opcoesPalestrante,
  onDesignar,
  designando,
}: {
  palestra: PalestraDoEncontro
  podeDesignar: boolean
  opcoesPalestrante: { value: string; label: string }[]
  onDesignar: (palestraId: string, palestranteId: string) => void
  designando: boolean
}) {
  const [selecionado, setSelecionado] = useState('')

  return (
    <div className="flex flex-col gap-3 border-b border-slate-100 py-4 last:border-b-0 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p className="text-sm font-medium text-slate-900">
          {palestra.numero}. {palestra.tema}
        </p>
        {palestra.palestranteNome ? (
          <div className="mt-1 flex items-center gap-2">
            <span className="text-sm text-slate-600">{palestra.palestranteNome}</span>
            {palestra.palestranteTipo && <Badge tone="info">{TIPO_PALESTRANTE_LABEL[palestra.palestranteTipo]}</Badge>}
          </div>
        ) : (
          <p className="mt-1 text-sm text-slate-400">Nenhum palestrante designado.</p>
        )}
      </div>

      {podeDesignar && (
        <div className="flex items-center gap-2">
          <Select
            value={selecionado}
            placeholder="Selecionar palestrante..."
            options={opcoesPalestrante}
            onChange={(event) => setSelecionado(event.target.value)}
          />
          <Button
            variant="secondary"
            disabled={!selecionado || designando}
            onClick={() => onDesignar(palestra.palestraId, selecionado)}
          >
            Designar
          </Button>
        </div>
      )}
    </div>
  )
}

/** Os 7 temas oficiais do encontro, em ordem fixa, com o palestrante designado em cada um (`screen_palestras`, RN-CONTEUDO-01). */
export function LecturesScreen() {
  const { id: encontroId } = useParams<{ id: string }>()
  const { perfilAtivo } = useProfile()
  const podeDesignar = perfilAtivo === 'PAROQUIA' || perfilAtivo === 'EQUIPE_COMANDO'

  const { data: palestras, isLoading, isError, refetch } = usePalestras(encontroId ?? '')
  const { data: palestrantes } = usePalestrantes()
  const designar = useDesignarPalestrante(encontroId ?? '')

  const opcoesPalestrante = (palestrantes?.content ?? []).map((palestrante) => ({
    value: palestrante.id,
    label: `${palestrante.nome} (${TIPO_PALESTRANTE_LABEL[palestrante.tipo]})`,
  }))

  return (
    <div className="p-6">
      <Link to={`/encontros/${encontroId}`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Detalhes do Encontro
      </Link>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-900">Palestras</h1>
        <Link to={`/encontros/${encontroId}/palestrantes`} className="text-sm font-medium text-violet-600 hover:underline">
          Palestrantes →
        </Link>
      </div>

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={palestras?.length === 0}
        onRetry={() => refetch()}
        skeletonRows={1}
        skeletonHeight="160px"
        errorMessage="Não foi possível carregar as palestras deste encontro."
        emptyMessage="Nenhuma palestra cadastrada para este encontro."
      >
        <div className="rounded-xl border border-slate-200 bg-white px-4">
          {(palestras ?? []).map((palestra) => (
            <LinhaPalestra
              key={palestra.palestraId}
              palestra={palestra}
              podeDesignar={podeDesignar}
              opcoesPalestrante={opcoesPalestrante}
              designando={designar.isPending}
              onDesignar={(palestraId, palestranteId) => designar.mutate({ palestraId, palestranteId })}
            />
          ))}
        </div>
      </ListState>
    </div>
  )
}
