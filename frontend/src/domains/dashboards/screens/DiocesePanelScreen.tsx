import { Link } from 'react-router-dom'
import { BarChart, KpiCard, KpiRow, ListState, PageHeader, PanelCard } from '../../../shared/ui'
import { useDashboardDiocese } from '../hooks/useDashboardDiocese'

/**
 * Painel da Diocese (`dash_diocese`, RF-053, RN-DASH-01). O cartão "Solicitações ao setorial" conta
 * transferências pendentes e empréstimos em aberto da diocese; aprovações de Encontro não entram
 * porque não existe fluxo de aprovação de Encontro no modelo.
 */
export function DiocesePanelScreen() {
  const { data: painel, isLoading, isError, refetch } = useDashboardDiocese()

  return (
    <div className="p-6">
      <PageHeader
        titulo="Painel da Diocese"
        subtitulo="Seguidores, setores e Encontros da sua diocese no ano corrente"
        acoes={
          <Link to="/hierarquia/paroquias" className="text-sm font-medium text-violet-600 hover:underline">
            Ver paróquias
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
            <KpiCard rotulo="Seguidores" valor={painel.seguidores} />
            <KpiCard rotulo="Setores" valor={painel.setores} />
            <KpiCard rotulo="Paróquias" valor={painel.paroquias} />
            <KpiCard rotulo="Encontros no ano" valor={painel.encontrosNoAno} nota="Ano civil corrente" />
          </KpiRow>

          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <BarChart
              titulo="Seguidores por setor"
              itens={painel.seguidoresPorSetor.map((setor) => ({ rotulo: setor.nome, valor: setor.total }))}
              mensagemVazio="Nenhum setor cadastrado na diocese."
            />

            <PanelCard
              titulo="Solicitações ao setorial"
              acao={
                <Link to="/setorial/solicitacoes" className="text-sm font-medium text-violet-600 hover:underline">
                  Ver solicitações
                </Link>
              }
            >
              <p className="text-3xl font-semibold text-slate-900">{painel.solicitacoesAoSetorial}</p>
              <p className="mt-1 text-sm text-slate-500">
                Transferências pendentes e empréstimos em aberto aguardando decisão do setorial.
              </p>
            </PanelCard>
          </div>
        </div>
      )}
    </div>
  )
}
