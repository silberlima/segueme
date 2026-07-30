import { Link } from 'react-router-dom'
import { Badge, BarChart, Button, KpiCard, KpiRow, PageHeader, PanelCard, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { PanelState } from '../components/PanelState'
import { formatarDataHora } from '../domain/dashboard.entity'
import type { ConviteDoPainel } from '../domain/dashboard.types'
import { useDashboardComando } from '../hooks/useDashboardComando'

const statusTone = {
  PENDENTE: 'warning',
  ACEITO: 'success',
  RECUSADO: 'danger',
} as const

const statusLabel = {
  PENDENTE: 'Pendente',
  ACEITO: 'Aceito',
  RECUSADO: 'Recusado',
} as const

/** Painel de Comando (`dash_comando`, RF-053, RN-DASH-01): acompanhamento dos convites do Encontro. */
export function CommandPanelScreen() {
  const { data: painel, isLoading, isError } = useDashboardComando()
  const encontro = painel?.encontroCorrente ?? null

  const colunas: TableColumn<ConviteDoPainel>[] = [
    { key: 'pessoa', header: 'Pessoa', render: (convite) => convite.pessoaNome },
    {
      key: 'equipe',
      header: 'Equipe / papel',
      render: (convite) =>
        `${convite.equipeNome} · ${convite.papel === 'COORDENADOR' ? 'Coordenador' : 'Integrante'}`,
    },
    { key: 'enviado', header: 'Enviado em', render: (convite) => formatarDataHora(convite.dataEnvioConvite) },
    {
      key: 'status',
      header: 'Status',
      render: (convite) => <Badge tone={statusTone[convite.status]}>{statusLabel[convite.status]}</Badge>,
    },
  ]

  return (
    <div className="p-6">
      <PageHeader
        titulo="Painel de Comando"
        subtitulo={
          encontro
            ? `${encontro.numero}º Encontro (${encontro.padroeiro}) · ${encontro.paroquiaNome}`
            : 'Sem Encontro em preparação'
        }
        acoes={
          encontro && (
            <Link to={`/encontros/${encontro.id}/convites`}>
              <Button>Novo convite</Button>
            </Link>
          )
        }
      />

      <PanelState isLoading={isLoading} isError={isError} />

      {painel && (
        <div className="flex flex-col gap-6">
          <KpiRow>
            <KpiCard rotulo="Convites enviados" valor={painel.convitesEnviados} />
            <KpiCard rotulo="Aceites" valor={painel.aceites} tone="positivo" />
            <KpiCard
              rotulo="Pendentes"
              valor={painel.pendentes}
              tone={painel.pendentes > 0 ? 'atencao' : 'neutro'}
            />
            <KpiCard rotulo="Recusas" valor={painel.recusas} tone={painel.recusas > 0 ? 'critico' : 'neutro'} />
          </KpiRow>

          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <PanelCard
              titulo="Acompanhamento de convites"
              acao={
                encontro && (
                  <Link
                    to={`/encontros/${encontro.id}/convites`}
                    className="text-sm font-medium text-violet-600 hover:underline"
                  >
                    Ver todos
                  </Link>
                )
              }
            >
              {painel.convitesRecentes.length === 0 ? (
                <p className="text-sm text-slate-500">Nenhum convite emitido neste Encontro.</p>
              ) : (
                <div className="overflow-x-auto">
                  <Table columns={colunas} rows={painel.convitesRecentes} getRowKey={(convite) => convite.id} />
                </div>
              )}
            </PanelCard>

            <BarChart
              titulo="Aceites por equipe"
              itens={painel.aceitesPorEquipe.map((equipe) => ({
                rotulo: `${equipe.equipeNome} (${equipe.aceitos}/${equipe.convidados})`,
                valor: equipe.aceitos,
              }))}
              mensagemVazio="Nenhum convite em aberto por equipe."
            />
          </div>
        </div>
      )}
    </div>
  )
}
