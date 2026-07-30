import { Link } from 'react-router-dom'
import { BarChart, KpiCard, KpiRow, ListState, PageHeader, PanelCard, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { rotuloRegiao } from '../domain/dashboard.entity'
import type { DioceseDestaque } from '../domain/dashboard.types'
import { useDashboardNacional } from '../hooks/useDashboardNacional'

/**
 * Painel Nacional (`dash_nacional`, RF-053, RN-DASH-01): visão do país inteiro, exclusiva do perfil
 * Nacional. O botão "Relatório anual" ainda não tem formato de saída definido
 * (`sistema-relatorios-config.md`, Fase 13) — por isso aponta para a tela de relatórios.
 */
export function NationalPanelScreen() {
  const { data: painel, isLoading, isError, refetch } = useDashboardNacional()

  const columns: TableColumn<DioceseDestaque>[] = [
    { key: 'nome', header: 'Diocese', render: (diocese) => diocese.nome },
    { key: 'paroquias', header: 'Paróquias', render: (diocese) => diocese.paroquias },
    { key: 'seguidores', header: 'Seguidores', render: (diocese) => diocese.seguidores },
    { key: 'encontros', header: 'Encontros no ano', render: (diocese) => diocese.encontrosNoAno },
  ]

  return (
    <div className="p-6">
      <PageHeader
        titulo="Painel Nacional"
        subtitulo="Seguidores, dioceses e Encontros do Brasil no ano corrente"
        acoes={
          <Link
            to="/hierarquia/dioceses"
            className="text-sm font-medium text-violet-600 hover:underline"
          >
            Ver dioceses
          </Link>
        }
      />

      <ListState
        isLoading={isLoading}
        isError={isError}
        onRetry={() => refetch()}
        skeletonRows={1}
        skeletonHeight="120px"
        errorMessage="Não foi possível carregar o painel. Tente novamente."
      />

      {painel && (
        <div className="flex flex-col gap-6">
          <KpiRow>
            <KpiCard rotulo="Seguidores no Brasil" valor={painel.seguidores} />
            <KpiCard rotulo="Dioceses" valor={painel.dioceses} />
            <KpiCard rotulo="Paróquias" valor={painel.paroquias} />
            <KpiCard rotulo="Encontros no ano" valor={painel.encontrosNoAno} nota="Ano civil corrente" />
          </KpiRow>

          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <BarChart
              titulo="Encontros por região"
              itens={painel.encontrosPorRegiao.map((contagem) => ({
                rotulo: rotuloRegiao(contagem.regiao),
                valor: contagem.total,
              }))}
            />

            <PanelCard
              titulo="Dioceses em destaque"
              acao={
                <Link to="/hierarquia/dioceses" className="text-sm font-medium text-violet-600 hover:underline">
                  Ver todas
                </Link>
              }
            >
              {painel.diocesesDestaque.length === 0 ? (
                <p className="text-sm text-slate-500">Nenhuma diocese cadastrada.</p>
              ) : (
                <div className="overflow-x-auto">
                  <Table
                    columns={columns}
                    rows={painel.diocesesDestaque}
                    getRowKey={(diocese) => diocese.dioceseId}
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
