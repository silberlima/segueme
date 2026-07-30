import { Link, useParams } from 'react-router-dom'
import { useProfile } from '../../../app/providers/ProfileProvider'
import { Badge, Button, ListState, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { SITUACAO_DESPESA_LABEL } from '../domain/financeiro.types'
import type { LancamentoFinanceiro } from '../domain/financeiro.types'
import { useAprovarLancamento } from '../hooks/useAprovarLancamento'
import { useLancamentos } from '../hooks/useLancamentos'
import { usePagarLancamento } from '../hooks/usePagarLancamento'

const formatador = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

/**
 * Despesas com controle de situação (`screen_despesas`, RF-049, RN-FINANCEIRO-02). Aprovar/pagar
 * liberado à Equipe Finanças e à Paróquia (pendência #12 de `pendencias-gerais.md`).
 */
export function ExpensesScreen() {
  const { id: encontroId } = useParams<{ id: string }>()
  const { perfilAtivo } = useProfile()
  const podeDecidir = perfilAtivo === 'EQUIPE_FINANCAS' || perfilAtivo === 'PAROQUIA'

  const { data: lancamentos, isLoading, isError, refetch } = useLancamentos(encontroId ?? '', { tipo: 'DESPESA' })
  const aprovar = useAprovarLancamento()
  const pagar = usePagarLancamento()

  const columns: TableColumn<LancamentoFinanceiro>[] = [
    { key: 'categoria', header: 'Categoria', render: (l) => l.categoria },
    { key: 'valor', header: 'Valor', render: (l) => formatador.format(l.valor) },
    { key: 'data', header: 'Data', render: (l) => new Date(l.data).toLocaleDateString('pt-BR') },
    {
      key: 'situacao',
      header: 'Situação',
      render: (l) => (
        <Badge tone={l.situacao === 'PAGA' ? 'success' : l.situacao === 'APROVADA' ? 'info' : 'warning'}>
          {l.situacao ? SITUACAO_DESPESA_LABEL[l.situacao] : '—'}
        </Badge>
      ),
    },
    {
      key: 'acoes',
      header: 'Ações',
      render: (l) => {
        if (!podeDecidir) return null
        if (l.situacao === 'PENDENTE') {
          return (
            <Button disabled={aprovar.isPending} onClick={() => aprovar.mutate(l.id)}>
              Aprovar
            </Button>
          )
        }
        if (l.situacao === 'APROVADA') {
          return (
            <Button disabled={pagar.isPending} onClick={() => pagar.mutate(l.id)}>
              Pagar
            </Button>
          )
        }
        return null
      },
    },
  ]

  return (
    <div className="p-6">
      <Link to={`/encontros/${encontroId}/financeiro`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Financeiro
      </Link>
      <h1 className="mb-6 text-xl font-semibold text-slate-900">Despesas</h1>

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={lancamentos?.content.length === 0}
        onRetry={() => refetch()}
        skeletonRows={1}
        skeletonHeight="120px"
        errorMessage="Não foi possível carregar as despesas."
        emptyMessage="Nenhuma despesa registrada."
      >
        <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
          <Table columns={columns} rows={lancamentos?.content ?? []} getRowKey={(l) => l.id} />
        </div>
      </ListState>
    </div>
  )
}
