import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Badge, Button, Select, Skeleton, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { STATUS_ENCONTRO_LABEL } from '../domain/encontro.types'
import type { EncontroResumo, StatusEncontro } from '../domain/encontro.types'
import { useEncontros } from '../hooks/useEncontros'

const STATUS_TONE: Record<StatusEncontro, 'success' | 'neutral'> = {
  ATIVO: 'success',
  ENCERRADO: 'neutral',
}

const PAGE_SIZE = 20

export function EncounterListScreen() {
  const navigate = useNavigate()
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)

  const { data, isLoading, isError, refetch } = useEncontros(
    { status: (status || undefined) as StatusEncontro | undefined },
    page,
    PAGE_SIZE,
  )

  const columns: TableColumn<EncontroResumo>[] = [
    {
      key: 'encontro',
      header: 'Encontro',
      render: (encontro) => (
        <button
          type="button"
          onClick={() => navigate(`/encontros/${encontro.id}`)}
          className="text-left hover:underline"
        >
          <span className="block font-medium text-slate-900">
            #{encontro.numero} · {encontro.padroeiro}
          </span>
          <span className="block text-xs text-slate-500">{encontro.paroquiaNome}</span>
        </button>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (encontro) => <Badge tone={STATUS_TONE[encontro.status]}>{STATUS_ENCONTRO_LABEL[encontro.status]}</Badge>,
    },
    { key: 'fase', header: 'Fase atual', render: (encontro) => encontro.faseAtual },
    {
      key: 'datas',
      header: 'Datas',
      render: (encontro) => `${encontro.dataInicio} — ${encontro.dataTermino}`,
    },
    { key: 'local', header: 'Local', render: (encontro) => encontro.local },
    {
      key: 'coordenador',
      header: 'Coordenador geral',
      render: (encontro) => encontro.coordenadorGeralNome ?? '—',
    },
  ]

  return (
    <div className="p-6">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="mb-1 text-xl font-semibold text-slate-900">Encontros</h1>
          <p className="text-sm text-slate-500">Encontros da paróquia e sua fase de operação.</p>
        </div>
        <Button onClick={() => navigate('/encontros/novo')}>Novo encontro</Button>
      </div>

      <div className="mb-4 flex flex-wrap items-end gap-3">
        <div className="w-56">
          <Select
            label="Status"
            value={status}
            options={[
              { value: '', label: 'Todos os status' },
              { value: 'ATIVO', label: STATUS_ENCONTRO_LABEL.ATIVO },
              { value: 'ENCERRADO', label: STATUS_ENCONTRO_LABEL.ENCERRADO },
            ]}
            onChange={(event) => {
              setPage(0)
              setStatus(event.target.value)
            }}
          />
        </div>
      </div>

      {isLoading && (
        <div className="flex flex-col gap-2">
          {Array.from({ length: 5 }).map((_, index) => (
            <Skeleton key={index} variant="rect" height="52px" />
          ))}
        </div>
      )}

      {isError && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          <p className="mb-3">Não foi possível carregar os encontros. Tente novamente mais tarde.</p>
          <Button variant="secondary" onClick={() => refetch()}>
            Tentar novamente
          </Button>
        </div>
      )}

      {data && data.content.length === 0 && (
        <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          <p className="mb-3">Nenhum encontro encontrado para os filtros aplicados.</p>
          <div className="flex gap-2">
            <Button
              variant="secondary"
              onClick={() => {
                setStatus('')
                setPage(0)
              }}
            >
              Limpar filtros
            </Button>
            <Button onClick={() => navigate('/encontros/novo')}>Novo encontro</Button>
          </div>
        </div>
      )}

      {data && data.content.length > 0 && (
        <>
          <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
            <Table columns={columns} rows={data.content} getRowKey={(encontro) => encontro.id} />
          </div>
          <div className="mt-4 flex items-center justify-between text-sm text-slate-500">
            <span>
              Página {data.page + 1} de {Math.max(data.totalPages, 1)} · {data.totalElements} encontros
            </span>
            <div className="flex gap-2">
              <Button variant="secondary" disabled={page === 0} onClick={() => setPage((current) => current - 1)}>
                Anterior
              </Button>
              <Button
                variant="secondary"
                disabled={data.page + 1 >= data.totalPages}
                onClick={() => setPage((current) => current + 1)}
              >
                Próxima
              </Button>
            </div>
          </div>
        </>
      )}
    </div>
  )
}
