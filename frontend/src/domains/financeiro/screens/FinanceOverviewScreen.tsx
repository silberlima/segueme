import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useProfile } from '../../../app/providers/ProfileProvider'
import { Badge, Button, ListState, Select, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { RevenueExpenseChart } from '../components/RevenueExpenseChart'
import { SITUACAO_DESPESA_LABEL, TIPO_LANCAMENTO_LABEL } from '../domain/financeiro.types'
import type { LancamentoFinanceiro, TipoLancamento } from '../domain/financeiro.types'
import { useLancamentos } from '../hooks/useLancamentos'
import { useRegistrarLancamento } from '../hooks/useRegistrarLancamento'
import { useResumoFinanceiro } from '../hooks/useResumoFinanceiro'

const formatador = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

/**
 * Visão geral financeira de um Encontro (`screen_financeiro`, RF-047, RN-FINANCEIRO-04). Registro
 * de lançamento restrito à Equipe Finanças; Paróquia acessa em modo consulta (`financeiro.md` 2.1).
 */
export function FinanceOverviewScreen() {
  const { id: encontroId } = useParams<{ id: string }>()
  const { perfilAtivo } = useProfile()
  const isEquipeFinancas = perfilAtivo === 'EQUIPE_FINANCAS'

  const [tipo, setTipo] = useState<TipoLancamento>('RECEITA')
  const [categoria, setCategoria] = useState('')
  const [valor, setValor] = useState('')
  const [data, setData] = useState(() => new Date().toISOString().slice(0, 10))

  const { data: resumo, isLoading: isLoadingResumo } = useResumoFinanceiro(encontroId ?? '')
  const { data: lancamentos, isLoading: isLoadingLancamentos } = useLancamentos(encontroId ?? '')
  const registrar = useRegistrarLancamento()

  const formularioValido = Boolean(categoria.trim()) && Number(valor) > 0

  function registrarLancamento() {
    if (!encontroId || !formularioValido) return
    registrar.mutate(
      { encontroId, tipo, categoria: categoria.trim(), valor: Number(valor), data },
      { onSuccess: () => { setCategoria(''); setValor('') } },
    )
  }

  const ultimosLancamentos = lancamentos?.content.slice(0, 5) ?? []

  const columns: TableColumn<LancamentoFinanceiro>[] = [
    { key: 'categoria', header: 'Categoria', render: (l) => l.categoria },
    { key: 'tipo', header: 'Tipo', render: (l) => TIPO_LANCAMENTO_LABEL[l.tipo] },
    { key: 'valor', header: 'Valor', render: (l) => formatador.format(l.valor) },
    { key: 'data', header: 'Data', render: (l) => new Date(l.data).toLocaleDateString('pt-BR') },
    {
      key: 'situacao',
      header: 'Situação',
      render: (l) =>
        l.situacao ? (
          <Badge tone={l.situacao === 'PAGA' ? 'success' : l.situacao === 'APROVADA' ? 'info' : 'warning'}>
            {SITUACAO_DESPESA_LABEL[l.situacao]}
          </Badge>
        ) : (
          '—'
        ),
    },
  ]

  return (
    <div className="p-6">
      <h1 className="mb-6 text-xl font-semibold text-slate-900">Financeiro</h1>

      <div className="mb-6 flex gap-3">
        <Link to={`/encontros/${encontroId}/financeiro/receitas`} className="text-sm font-medium text-violet-600 hover:underline">
          Receitas
        </Link>
        <Link to={`/encontros/${encontroId}/financeiro/despesas`} className="text-sm font-medium text-violet-600 hover:underline">
          Despesas
        </Link>
        <Link to={`/encontros/${encontroId}/financeiro/campanhas`} className="text-sm font-medium text-violet-600 hover:underline">
          Campanhas
        </Link>
      </div>

      <ListState isLoading={isLoadingResumo} skeletonRows={1} skeletonHeight="100px" />

      {resumo && (
        <div className="mb-6 grid grid-cols-2 gap-3 sm:grid-cols-4">
          <div className="rounded-xl border border-slate-200 bg-white p-4 text-center">
            <p className="text-2xl font-semibold text-slate-900">{formatador.format(resumo.saldo)}</p>
            <p className="text-xs text-slate-500">Saldo atual</p>
          </div>
          <div className="rounded-xl border border-slate-200 bg-white p-4 text-center">
            <p className="text-2xl font-semibold text-slate-900">{formatador.format(resumo.totalReceitas)}</p>
            <p className="text-xs text-slate-500">Receitas</p>
          </div>
          <div className="rounded-xl border border-slate-200 bg-white p-4 text-center">
            <p className="text-2xl font-semibold text-slate-900">{formatador.format(resumo.totalDespesas)}</p>
            <p className="text-xs text-slate-500">Despesas</p>
          </div>
          <div className="rounded-xl border border-slate-200 bg-white p-4 text-center">
            <p className="text-2xl font-semibold text-slate-900">{resumo.campanhasAtivas}</p>
            <p className="text-xs text-slate-500">Campanhas ativas</p>
          </div>
        </div>
      )}

      {resumo && (
        <div className="mb-8">
          <RevenueExpenseChart totalReceitas={resumo.totalReceitas} totalDespesas={resumo.totalDespesas} />
        </div>
      )}

      {isEquipeFinancas && (
        <div className="mb-8 rounded-xl border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-900">Lançamento</h2>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-4">
            <Select
              label="Tipo"
              value={tipo}
              options={[
                { value: 'RECEITA', label: 'Receita' },
                { value: 'DESPESA', label: 'Despesa' },
              ]}
              onChange={(event) => setTipo(event.target.value as TipoLancamento)}
            />

            <label className="flex flex-col gap-1.5">
              <span className="text-sm font-medium text-slate-700">Categoria</span>
              <input
                type="text"
                value={categoria}
                onChange={(event) => setCategoria(event.target.value)}
                className="h-10 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none focus:border-violet-500"
              />
            </label>

            <label className="flex flex-col gap-1.5">
              <span className="text-sm font-medium text-slate-700">Valor</span>
              <input
                type="number"
                min="0"
                step="0.01"
                value={valor}
                onChange={(event) => setValor(event.target.value)}
                className="h-10 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none focus:border-violet-500"
              />
            </label>

            <label className="flex flex-col gap-1.5">
              <span className="text-sm font-medium text-slate-700">Data</span>
              <input
                type="date"
                value={data}
                onChange={(event) => setData(event.target.value)}
                className="h-10 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none focus:border-violet-500"
              />
            </label>
          </div>

          <div className="mt-4 flex justify-end">
            <Button disabled={!formularioValido || registrar.isPending} onClick={registrarLancamento}>
              Registrar lançamento
            </Button>
          </div>
        </div>
      )}

      <h2 className="mb-3 text-sm font-semibold text-slate-900">Últimas movimentações</h2>

      <ListState
        isLoading={isLoadingLancamentos}
        isEmpty={ultimosLancamentos.length === 0}
        skeletonRows={1}
        skeletonHeight="120px"
        emptyMessage="Nenhuma movimentação registrada."
      >
        <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
          <Table columns={columns} rows={ultimosLancamentos} getRowKey={(l) => l.id} />
        </div>
      </ListState>
    </div>
  )
}
