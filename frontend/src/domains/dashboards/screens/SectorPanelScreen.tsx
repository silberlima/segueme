import { Link } from 'react-router-dom'
import { Badge, KpiCard, KpiRow, PageHeader, PanelCard, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { PanelState } from '../components/PanelState'
import { formatarData, formatarDataHora } from '../domain/dashboard.entity'
import type { ParoquiaDoSetor, SolicitacaoDoPainel } from '../domain/dashboard.types'
import { useDashboardSetorial } from '../hooks/useDashboardSetorial'

/**
 * Painel do Setor (`dash_setorial`, RF-053, RN-DASH-01). Aprovar/recusar é a mesma ação central do
 * inbox `screen_solicitacoes` (`transferencias-e-emprestimos.md` 2.3): o painel lista e leva para
 * lá, em vez de duplicar a decisão em dois lugares.
 */
export function SectorPanelScreen() {
  const { data: painel, isLoading, isError } = useDashboardSetorial()

  const colunasSolicitacoes: TableColumn<SolicitacaoDoPainel>[] = [
    {
      key: 'tipo',
      header: 'Tipo',
      render: (solicitacao) => (
        <Badge tone={solicitacao.tipo === 'TRANSFERENCIA' ? 'info' : 'warning'}>
          {solicitacao.tipo === 'TRANSFERENCIA' ? 'Transferência' : 'Empréstimo'}
        </Badge>
      ),
    },
    { key: 'descricao', header: 'Descrição', render: (solicitacao) => solicitacao.descricao },
    { key: 'recebida', header: 'Recebida em', render: (solicitacao) => formatarDataHora(solicitacao.createdAt) },
    {
      key: 'decidir',
      header: '',
      render: () => (
        <Link to="/setorial/solicitacoes" className="text-sm font-medium text-violet-600 hover:underline">
          Decidir
        </Link>
      ),
    },
  ]

  const colunasParoquias: TableColumn<ParoquiaDoSetor>[] = [
    { key: 'nome', header: 'Paróquia', render: (paroquia) => paroquia.nome },
    { key: 'seguidores', header: 'Seguidores', render: (paroquia) => paroquia.seguidores },
    {
      key: 'proximo',
      header: 'Próximo encontro',
      render: (paroquia) =>
        paroquia.proximoEncontro ? formatarData(paroquia.proximoEncontro) : 'Sem encontro em aberto',
    },
  ]

  return (
    <div className="p-6">
      <PageHeader
        titulo="Painel do Setor"
        subtitulo="Solicitações do setor e situação das suas paróquias"
        acoes={
          <Link to="/setorial/solicitacoes" className="text-sm font-medium text-violet-600 hover:underline">
            Inbox de solicitações
          </Link>
        }
      />

      <PanelState isLoading={isLoading} isError={isError} />

      {painel && (
        <div className="flex flex-col gap-6">
          <KpiRow>
            <KpiCard
              rotulo="Solicitações pendentes"
              valor={painel.solicitacoesPendentes}
              tone={painel.solicitacoesPendentes > 0 ? 'atencao' : 'neutro'}
            />
            <KpiCard rotulo="Transferências no mês" valor={painel.transferenciasNoMes} nota="Mês civil corrente" />
            <KpiCard rotulo="Empréstimos ativos" valor={painel.emprestimosAtivos} />
            <KpiCard rotulo="Seguidores no setor" valor={painel.seguidores} />
          </KpiRow>

          <PanelCard
            titulo="Solicitações pendentes de aprovação"
            acao={
              <Link to="/setorial/solicitacoes" className="text-sm font-medium text-violet-600 hover:underline">
                Ver todas
              </Link>
            }
          >
            {painel.solicitacoes.length === 0 ? (
              <p className="text-sm text-slate-500">Nenhuma solicitação aguardando decisão.</p>
            ) : (
              <div className="overflow-x-auto">
                <Table
                  columns={colunasSolicitacoes}
                  rows={painel.solicitacoes}
                  getRowKey={(solicitacao) => solicitacao.id}
                />
              </div>
            )}
          </PanelCard>

          <PanelCard
            titulo="Paróquias do setor"
            acao={
              <Link to="/hierarquia/paroquias" className="text-sm font-medium text-violet-600 hover:underline">
                Ver todas
              </Link>
            }
          >
            {painel.paroquias.length === 0 ? (
              <p className="text-sm text-slate-500">Nenhuma paróquia cadastrada no setor.</p>
            ) : (
              <div className="overflow-x-auto">
                <Table
                  columns={colunasParoquias}
                  rows={painel.paroquias}
                  getRowKey={(paroquia) => paroquia.paroquiaId}
                />
              </div>
            )}
          </PanelCard>
        </div>
      )}
    </div>
  )
}
