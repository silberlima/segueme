import { Link } from 'react-router-dom'
import { Button, KpiCard, KpiRow, ListState, PageHeader, PanelCard, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { formatarDataHora } from '../domain/dashboard.entity'
import type { PreInscricaoDoPainel, ReservaDoPainel } from '../domain/dashboard.types'
import { useDashboardFichas } from '../hooks/useDashboardFichas'

/**
 * Painel de Fichas (`dash_fichas`, RF-053, RN-DASH-01): funil de inscrição do Encontro corrente,
 * pré-inscrições aguardando ficha e fila de reserva por prioridade (RN-INSCRICAO-04).
 */
export function FormsPanelScreen() {
  const { data: painel, isLoading, isError, refetch } = useDashboardFichas()
  const encontro = painel?.encontroCorrente ?? null

  const colunasPreInscricoes: TableColumn<PreInscricaoDoPainel>[] = [
    { key: 'nome', header: 'Candidato', render: (preInscricao) => preInscricao.pessoaNome },
    { key: 'idade', header: 'Idade', render: (preInscricao) => preInscricao.idade },
    { key: 'recebida', header: 'Recebida em', render: (preInscricao) => formatarDataHora(preInscricao.recebidaEm) },
    {
      key: 'preencher',
      header: '',
      render: (preInscricao) =>
        encontro && (
          <Link
            to={`/encontros/${encontro.id}/inscricoes/${preInscricao.id}/ficha`}
            className="text-sm font-medium text-violet-600 hover:underline"
          >
            Preencher
          </Link>
        ),
    },
  ]

  const colunasReserva: TableColumn<ReservaDoPainel>[] = [
    { key: 'ordem', header: 'Ordem', render: (reserva) => reserva.ordem },
    { key: 'nome', header: 'Candidato', render: (reserva) => reserva.pessoaNome },
    { key: 'paroquia', header: 'Paróquia', render: (reserva) => reserva.paroquiaNome },
    { key: 'desde', header: 'Na fila desde', render: (reserva) => formatarDataHora(reserva.naFilaDesde) },
  ]

  return (
    <div className="p-6">
      <PageHeader
        titulo="Painel de Fichas"
        subtitulo={
          encontro
            ? `${encontro.numero}º Encontro (${encontro.padroeiro}) · ${encontro.vagas} vagas`
            : 'Sem Encontro em preparação'
        }
        acoes={
          encontro && (
            <Link to={`/encontros/${encontro.id}/inscricoes`}>
              <Button>Nova ficha</Button>
            </Link>
          )
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
            <KpiCard
              rotulo="Aguardando ficha"
              valor={painel.aguardandoFicha}
              tone={painel.aguardandoFicha > 0 ? 'atencao' : 'neutro'}
            />
            <KpiCard rotulo="Confirmados" valor={painel.confirmados} />
            <KpiCard rotulo="Vagas restantes" valor={painel.vagasRestantes} />
            <KpiCard rotulo="Cadastro reserva" valor={painel.cadastroReserva} />
          </KpiRow>

          <PanelCard titulo="Fluxo de inscrição">
            <ol className="grid grid-cols-2 gap-3 sm:grid-cols-5">
              {[
                { etapa: 'Pré-inscrição', valor: painel.aguardandoFicha },
                { etapa: 'Em análise', valor: painel.emAnalise },
                { etapa: 'Confirmados', valor: painel.confirmados },
                { etapa: 'Seguimistas', valor: painel.seguimistas },
                { etapa: 'Seguidores', valor: painel.seguidores },
              ].map((passo) => (
                <li key={passo.etapa} className="rounded-lg border border-slate-200 p-3 text-center">
                  <p className="text-xl font-semibold text-slate-900">{passo.valor}</p>
                  <p className="text-xs text-slate-500">{passo.etapa}</p>
                </li>
              ))}
            </ol>
          </PanelCard>

          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <PanelCard
              titulo="Pré-inscrições aguardando ficha"
              acao={
                encontro && (
                  <Link
                    to={`/encontros/${encontro.id}/inscricoes`}
                    className="text-sm font-medium text-violet-600 hover:underline"
                  >
                    Ver todas
                  </Link>
                )
              }
            >
              {painel.preInscricoesAguardandoFicha.length === 0 ? (
                <p className="text-sm text-slate-500">Nenhuma pré-inscrição aguardando ficha.</p>
              ) : (
                <div className="overflow-x-auto">
                  <Table
                    columns={colunasPreInscricoes}
                    rows={painel.preInscricoesAguardandoFicha}
                    getRowKey={(preInscricao) => preInscricao.id}
                  />
                </div>
              )}
            </PanelCard>

            <PanelCard
              titulo="Cadastro de reserva"
              acao={
                encontro && (
                  <Link
                    to={`/encontros/${encontro.id}/reserva`}
                    className="text-sm font-medium text-violet-600 hover:underline"
                  >
                    Ver todos
                  </Link>
                )
              }
            >
              {painel.reserva.length === 0 ? (
                <p className="text-sm text-slate-500">Nenhum candidato na fila de reserva.</p>
              ) : (
                <div className="overflow-x-auto">
                  <Table columns={colunasReserva} rows={painel.reserva} getRowKey={(reserva) => reserva.id} />
                </div>
              )}
            </PanelCard>
          </div>
        </div>
      )}
    </div>
  )
}
