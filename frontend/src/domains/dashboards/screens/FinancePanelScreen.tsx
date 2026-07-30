import { Link } from 'react-router-dom'
import { Badge, Button, KpiCard, KpiRow, PageHeader, PanelCard, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { PanelState } from '../components/PanelState'
import { ProgressBar } from '../components/ProgressBar'
import { formatarData, formatarMoeda, percentualDaMeta } from '../domain/dashboard.entity'
import type { LancamentoDoPainel } from '../domain/dashboard.types'
import { useDashboardFinancas } from '../hooks/useDashboardFinancas'

/**
 * Painel Financeiro (`dash_financas`, RF-053, RN-DASH-01). O saldo é o mesmo calculado pela tela de
 * financeiro (RN-FINANCEIRO-04); "Extrato" ainda não tem formato de saída definido (Fase 13).
 */
export function FinancePanelScreen() {
  const { data: painel, isLoading, isError } = useDashboardFinancas()
  const encontro = painel?.encontroCorrente ?? null

  const colunas: TableColumn<LancamentoDoPainel>[] = [
    { key: 'categoria', header: 'Categoria', render: (lancamento) => lancamento.categoria },
    {
      key: 'tipo',
      header: 'Tipo',
      render: (lancamento) => (
        <Badge tone={lancamento.tipo === 'RECEITA' ? 'success' : 'danger'}>
          {lancamento.tipo === 'RECEITA' ? 'Receita' : 'Despesa'}
        </Badge>
      ),
    },
    { key: 'data', header: 'Data', render: (lancamento) => formatarData(lancamento.data) },
    {
      key: 'valor',
      header: 'Valor',
      render: (lancamento) =>
        `${lancamento.tipo === 'RECEITA' ? '+' : '−'} ${formatarMoeda(lancamento.valor)}`,
    },
  ]

  return (
    <div className="p-6">
      <PageHeader
        titulo="Painel Financeiro"
        subtitulo={
          encontro
            ? `${encontro.numero}º Encontro (${encontro.padroeiro}) · ${encontro.paroquiaNome}`
            : 'Sem Encontro em preparação'
        }
        acoes={
          encontro && (
            <Link to={`/encontros/${encontro.id}/financeiro`}>
              <Button>Lançamento</Button>
            </Link>
          )
        }
      />

      <PanelState isLoading={isLoading} isError={isError} />

      {painel && (
        <div className="flex flex-col gap-6">
          <KpiRow>
            <KpiCard
              rotulo="Saldo atual"
              valor={formatarMoeda(painel.resumo.saldo)}
              tone={painel.resumo.saldo < 0 ? 'critico' : 'positivo'}
            />
            <KpiCard rotulo="Receitas" valor={formatarMoeda(painel.resumo.totalReceitas)} />
            <KpiCard rotulo="Despesas" valor={formatarMoeda(painel.resumo.totalDespesas)} />
            <KpiCard rotulo="Campanhas ativas" valor={painel.resumo.campanhasAtivas} />
          </KpiRow>

          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <PanelCard
              titulo="Campanhas de arrecadação"
              acao={
                encontro && (
                  <Link
                    to={`/encontros/${encontro.id}/financeiro/campanhas`}
                    className="text-sm font-medium text-violet-600 hover:underline"
                  >
                    Ver todas
                  </Link>
                )
              }
            >
              {painel.campanhas.length === 0 ? (
                <p className="text-sm text-slate-500">Nenhuma campanha cadastrada.</p>
              ) : (
                <div className="flex flex-col gap-3">
                  {painel.campanhas.map((campanha) => (
                    <ProgressBar
                      key={campanha.id}
                      rotulo={campanha.nome}
                      detalhe={`${formatarMoeda(campanha.valorArrecadado)} de ${formatarMoeda(campanha.meta)}`}
                      percentual={percentualDaMeta(campanha.valorArrecadado, campanha.meta)}
                      tone="verde"
                    />
                  ))}
                </div>
              )}
            </PanelCard>

            <PanelCard
              titulo="Lançamentos recentes"
              acao={
                encontro && (
                  <Link
                    to={`/encontros/${encontro.id}/financeiro`}
                    className="text-sm font-medium text-violet-600 hover:underline"
                  >
                    Ver todos
                  </Link>
                )
              }
            >
              {painel.lancamentosRecentes.length === 0 ? (
                <p className="text-sm text-slate-500">Nenhum lançamento registrado.</p>
              ) : (
                <div className="overflow-x-auto">
                  <Table
                    columns={colunas}
                    rows={painel.lancamentosRecentes}
                    getRowKey={(lancamento) => lancamento.id}
                  />
                </div>
              )}
            </PanelCard>
          </div>
        </div>
      )}
    </div>
  )
}
