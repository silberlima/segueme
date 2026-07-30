import { Link } from 'react-router-dom'
import { Badge, BarChart, Button, KpiCard, KpiRow, PageHeader, PanelCard, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { PanelState } from '../components/PanelState'
import { formatarData, rotuloMesDoGrafico } from '../domain/dashboard.entity'
import type { FrequenciaDoPainel } from '../domain/dashboard.types'
import { useDashboardPosEncontro } from '../hooks/useDashboardPosEncontro'

const faixaTone = {
  ALTA: 'success',
  MEDIA: 'warning',
  BAIXA: 'danger',
} as const

const faixaLabel = {
  ALTA: 'Alta',
  MEDIA: 'Média',
  BAIXA: 'Baixa',
} as const

/**
 * Painel Pós-Encontro (`dash_posencontro`, RF-053, RN-DASH-01): frequência dos acompanhados e o
 * ranking de prioridade que alimenta o motor de sugestões da Montagem (RN-POS-02/03).
 */
export function PostEncounterPanelScreen() {
  const { data: painel, isLoading, isError } = useDashboardPosEncontro()
  const proximo = painel?.proximoEncontroPos ?? null

  const colunas: TableColumn<FrequenciaDoPainel>[] = [
    { key: 'pessoa', header: 'Jovem', render: (frequencia) => frequencia.pessoaNome },
    {
      key: 'presencas',
      header: 'Presenças',
      render: (frequencia) => `${frequencia.presencas}/${frequencia.totalEncontros}`,
    },
    { key: 'percentual', header: 'Frequência', render: (frequencia) => `${frequencia.percentual}%` },
    {
      key: 'faixa',
      header: 'Prioridade',
      render: (frequencia) => (
        <Badge tone={faixaTone[frequencia.faixaPrioridade]}>{faixaLabel[frequencia.faixaPrioridade]}</Badge>
      ),
    },
  ]

  return (
    <div className="p-6">
      <PageHeader
        titulo="Painel Pós-Encontro"
        subtitulo="Frequência dos jovens acompanhados e prioridade para as próximas equipes"
        acoes={
          <Link to="/pos-encontro/presencas">
            <Button>Registrar presença</Button>
          </Link>
        }
      />

      <PanelState isLoading={isLoading} isError={isError} />

      {painel && (
        <div className="flex flex-col gap-6">
          <KpiRow>
            <KpiCard rotulo="Jovens acompanhados" valor={painel.jovensAcompanhados} />
            <KpiCard
              rotulo="Presença média"
              valor={`${painel.presencaMedia}%`}
              tone={painel.presencaMedia >= 75 ? 'positivo' : painel.presencaMedia >= 40 ? 'atencao' : 'critico'}
            />
            <KpiCard rotulo="Encontros pós no mês" valor={painel.encontrosPosNoMes} nota="Mês civil corrente" />
            <KpiCard rotulo="Prioridade alta" valor={painel.prioridadeAlta} nota="Elegíveis às próximas equipes" />
          </KpiRow>

          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <BarChart
              titulo="Frequência nos encontros pós"
              itens={painel.frequenciaMensal.map((contagem) => ({
                rotulo: rotuloMesDoGrafico(contagem),
                valor: contagem.total,
              }))}
              mensagemVazio="Nenhuma presença registrada nos últimos meses."
            />

            <PanelCard
              titulo="Próximo encontro pós"
              acao={
                <Link to="/pos-encontro/presencas" className="text-sm font-medium text-violet-600 hover:underline">
                  Abrir chamada
                </Link>
              }
            >
              {!proximo ? (
                <p className="text-sm text-slate-500">Nenhum encontro pós agendado.</p>
              ) : (
                <div className="flex flex-col gap-2 text-sm">
                  <p className="text-2xl font-semibold text-slate-900">{formatarData(proximo.data)}</p>
                  <p className="text-slate-600">{proximo.tema ?? 'Tema a definir'}</p>
                  <p className="text-slate-500">
                    {painel.chamadaRegistrada} presença(s) já conferida(s) para este encontro.
                  </p>
                </div>
              )}
            </PanelCard>
          </div>

          <PanelCard
            titulo="Prioridade para próximos encontros"
            acao={
              <Link to="/pos-encontro/prioridades" className="text-sm font-medium text-violet-600 hover:underline">
                Ver todos
              </Link>
            }
          >
            {painel.prioridades.length === 0 ? (
              <p className="text-sm text-slate-500">Nenhum jovem acompanhado ainda.</p>
            ) : (
              <div className="overflow-x-auto">
                <Table
                  columns={colunas}
                  rows={painel.prioridades}
                  getRowKey={(frequencia) => frequencia.pessoaId}
                />
              </div>
            )}
          </PanelCard>
        </div>
      )}
    </div>
  )
}
