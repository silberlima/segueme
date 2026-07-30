import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Badge, Button, ListState, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import type { Participante } from '../domain/inscricao.types'
import { useAtualizarPagamento } from '../hooks/useAtualizarPagamento'
import { useFunil } from '../hooks/useFunil'
import { useParticipantes } from '../hooks/useParticipantes'

const ETAPAS_FUNIL: { key: 'preInscritos' | 'emAnalise' | 'confirmados' | 'emReserva' | 'seguidores'; label: string }[] = [
  { key: 'preInscritos', label: 'Pré-inscrição' },
  { key: 'emAnalise', label: 'Fichas' },
  { key: 'confirmados', label: 'Confirmados' },
  { key: 'emReserva', label: 'Reserva' },
  { key: 'seguidores', label: 'Seguidores' },
]

export function ParticipantsScreen() {
  const { id: encontroId } = useParams<{ id: string }>()
  const [page, setPage] = useState(0)

  const { data: funil } = useFunil(encontroId)
  const { data, isLoading, isError, refetch } = useParticipantes(encontroId, page)
  const atualizarPagamento = useAtualizarPagamento()

  const columns: TableColumn<Participante>[] = [
    { key: 'pessoa', header: 'Participante', render: (participante) => participante.pessoaNome },
    { key: 'paroquia', header: 'Paróquia', render: (participante) => participante.paroquiaNome },
    {
      key: 'pagamento',
      header: 'Pagamento',
      render: (participante) => (
        <div className="flex items-center gap-2">
          <Badge tone={participante.statusPagamento === 'PAGO' ? 'success' : 'warning'}>
            {participante.statusPagamento === 'PAGO' ? 'Pago' : 'Pendente'}
          </Badge>
          <Button
            variant="secondary"
            disabled={atualizarPagamento.isPending}
            onClick={() =>
              atualizarPagamento.mutate({
                fichaInscricaoId: participante.fichaInscricaoId,
                statusPagamento: participante.statusPagamento === 'PAGO' ? 'PENDENTE' : 'PAGO',
              })
            }
          >
            {participante.statusPagamento === 'PAGO' ? 'Marcar pendente' : 'Marcar pago'}
          </Button>
        </div>
      ),
    },
  ]

  return (
    <div className="p-6">
      <Link to={`/encontros/${encontroId}`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Detalhe do Encontro
      </Link>

      <h1 className="mb-6 text-xl font-semibold text-slate-900">Participantes</h1>

      {funil && (
        <div className="mb-6 grid grid-cols-2 gap-3 sm:grid-cols-5">
          {ETAPAS_FUNIL.map((etapa) => (
            <div key={etapa.key} className="rounded-xl border border-slate-200 bg-white p-4 text-center">
              <p className="text-2xl font-semibold text-slate-900">{funil[etapa.key]}</p>
              <p className="text-xs text-slate-500">{etapa.label}</p>
            </div>
          ))}
        </div>
      )}

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={data?.content.length === 0}
        onRetry={() => refetch()}
        skeletonRows={4}
        errorMessage="Não foi possível carregar os participantes. Tente novamente mais tarde."
        emptyMessage="Nenhum participante confirmado ainda."
      >
        <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
          <Table
            columns={columns}
            rows={data?.content ?? []}
            getRowKey={(participante) => participante.fichaInscricaoId}
          />
        </div>
        <div className="mt-4 flex items-center justify-between text-sm text-slate-500">
          <span>
            Página {(data?.page ?? 0) + 1} de {Math.max(data?.totalPages ?? 1, 1)} · {data?.totalElements ?? 0}{' '}
            participantes
          </span>
          <div className="flex gap-2">
            <Button variant="secondary" disabled={page === 0} onClick={() => setPage((current) => current - 1)}>
              Anterior
            </Button>
            <Button
              variant="secondary"
              disabled={(data?.page ?? 0) + 1 >= (data?.totalPages ?? 0)}
              onClick={() => setPage((current) => current + 1)}
            >
              Próxima
            </Button>
          </div>
        </div>
      </ListState>
    </div>
  )
}
