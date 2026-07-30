import { Link, useParams } from 'react-router-dom'
import { ListState, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { useLancamentos } from '../hooks/useLancamentos'

const formatador = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

interface TotalPorCategoria {
  categoria: string
  total: number
}

/** Receitas por categoria (`screen_receitas`, RF-048). */
export function RevenuesScreen() {
  const { id: encontroId } = useParams<{ id: string }>()
  const { data: lancamentos, isLoading, isError, refetch } = useLancamentos(encontroId ?? '', { tipo: 'RECEITA' })

  const totaisPorCategoria: TotalPorCategoria[] = Object.values(
    (lancamentos?.content ?? []).reduce<Record<string, TotalPorCategoria>>((acumulado, lancamento) => {
      const atual = acumulado[lancamento.categoria] ?? { categoria: lancamento.categoria, total: 0 }
      atual.total += lancamento.valor
      acumulado[lancamento.categoria] = atual
      return acumulado
    }, {}),
  )

  const columns: TableColumn<TotalPorCategoria>[] = [
    { key: 'categoria', header: 'Categoria', render: (item) => item.categoria },
    { key: 'total', header: 'Total', render: (item) => formatador.format(item.total) },
  ]

  return (
    <div className="p-6">
      <Link to={`/encontros/${encontroId}/financeiro`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Financeiro
      </Link>
      <h1 className="mb-6 text-xl font-semibold text-slate-900">Receitas</h1>

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={totaisPorCategoria.length === 0}
        onRetry={() => refetch()}
        skeletonRows={1}
        skeletonHeight="120px"
        errorMessage="Não foi possível carregar as receitas."
        emptyMessage="Nenhuma receita registrada."
      >
        <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
          <Table columns={columns} rows={totaisPorCategoria} getRowKey={(item) => item.categoria} />
        </div>
      </ListState>
    </div>
  )
}
