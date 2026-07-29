import { Link, useParams } from 'react-router-dom'
import { Button, Skeleton, Table } from '../../../shared/ui'
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

      {isLoading && <Skeleton variant="rect" height="120px" />}

      {isError && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          <p className="mb-3">Não foi possível carregar as receitas.</p>
          <Button variant="secondary" onClick={() => refetch()}>
            Tentar novamente
          </Button>
        </div>
      )}

      {!isLoading && totaisPorCategoria.length === 0 && (
        <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          Nenhuma receita registrada.
        </div>
      )}

      {totaisPorCategoria.length > 0 && (
        <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
          <Table columns={columns} rows={totaisPorCategoria} getRowKey={(item) => item.categoria} />
        </div>
      )}
    </div>
  )
}
