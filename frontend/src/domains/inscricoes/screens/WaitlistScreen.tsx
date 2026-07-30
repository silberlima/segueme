import { Link, useParams } from 'react-router-dom'
import { Button, ListState, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import type { CadastroReserva } from '../domain/inscricao.types'
import { useCadastroReserva } from '../hooks/useCadastroReserva'
import { usePromoverReserva } from '../hooks/usePromoverReserva'

export function WaitlistScreen() {
  const { id: encontroId } = useParams<{ id: string }>()
  const { data, isLoading, isError, refetch } = useCadastroReserva(encontroId)
  const promoverReserva = usePromoverReserva()

  const columns: TableColumn<CadastroReserva>[] = [
    { key: 'ordem', header: 'Ordem', render: (reserva) => `#${reserva.ordem}` },
    { key: 'pessoa', header: 'Pessoa', render: (reserva) => reserva.pessoaNome },
    { key: 'paroquia', header: 'Paróquia', render: (reserva) => reserva.paroquiaNome },
    {
      key: 'naFilaDesde',
      header: 'Na fila desde',
      render: (reserva) => new Date(reserva.naFilaDesde).toLocaleDateString('pt-BR'),
    },
    {
      key: 'acao',
      header: 'Ação',
      render: (reserva) => (
        <Button
          disabled={!encontroId || promoverReserva.isPending}
          onClick={() =>
            encontroId &&
            promoverReserva.mutate({ cadastroReservaId: reserva.id, encontroDestinoId: encontroId })
          }
        >
          Promover à vaga
        </Button>
      ),
    },
  ]

  return (
    <div className="p-6">
      <Link to={`/encontros/${encontroId}`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Detalhe do Encontro
      </Link>

      <h1 className="mb-1 text-xl font-semibold text-slate-900">Cadastro de Reserva</h1>
      <p className="mb-6 text-sm text-slate-500">
        Vagas esgotadas: a fila segue ordem de chegada e vale automaticamente para o próximo encontro da mesma
        paróquia.
      </p>

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={data?.length === 0}
        onRetry={() => refetch()}
        errorMessage="Não foi possível carregar o cadastro de reserva. Tente novamente mais tarde."
        emptyMessage="Nenhum candidato em reserva para a paróquia deste encontro."
      >
        <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
          <Table columns={columns} rows={data ?? []} getRowKey={(reserva) => reserva.id} />
        </div>
      </ListState>
    </div>
  )
}
