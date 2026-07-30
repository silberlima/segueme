import { Badge, Button, ListState, PageHeader, PanelCard, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { ReportTypeCard } from '../components/ReportTypeCard'
import { STATUS_LABEL, STATUS_TONE, formatarDataHora, podeBaixar } from '../domain/relatorio.entity'
import type { Relatorio } from '../domain/relatorio.types'
import { useReports } from '../hooks/useReports'

/**
 * Relatórios (`screen_relatorios`, RF-056, UC-22): os cartões de tipo liberados para o perfil ativo
 * e a lista "Gerados recentemente".
 *
 * <p>Gerar não trava a tela: o pedido volta como "Gerando..." e a lista se atualiza sozinha quando
 * o arquivo fica pronto — a geração roda no servidor, fora da requisição.
 */
export function ReportsScreen() {
  const {
    tipos,
    isLoadingTipos,
    relatorios,
    isLoadingRelatorios,
    isErrorRelatorios,
    recarregarRelatorios,
    gerar,
    tipoEmGeracao,
    baixar,
    idEmDownload,
  } = useReports()

  const colunas: TableColumn<Relatorio>[] = [
    { key: 'tipo', header: 'Relatório', render: (relatorio) => relatorio.tipoTitulo },
    {
      key: 'status',
      header: 'Situação',
      render: (relatorio) => <Badge tone={STATUS_TONE[relatorio.status]}>{STATUS_LABEL[relatorio.status]}</Badge>,
    },
    {
      key: 'linhas',
      header: 'Linhas',
      render: (relatorio) => (relatorio.totalLinhas === null ? '—' : String(relatorio.totalLinhas)),
    },
    { key: 'solicitadoEm', header: 'Solicitado em', render: (relatorio) => formatarDataHora(relatorio.solicitadoEm) },
    {
      key: 'acao',
      header: 'Arquivo',
      render: (relatorio) =>
        podeBaixar(relatorio) ? (
          <Button
            variant="secondary"
            disabled={idEmDownload === relatorio.id}
            onClick={() => baixar(relatorio.id)}
          >
            Baixar
          </Button>
        ) : (
          <span className="text-xs text-slate-500">{relatorio.erro ?? 'Aguardando geração'}</span>
        ),
    },
  ]

  return (
    <div className="p-6">
      <PageHeader
        titulo="Relatórios"
        subtitulo="Documentos consolidados a partir dos dados do seu escopo"
      />

      <div className="mb-6">
        <ListState
          isLoading={isLoadingTipos}
          isEmpty={tipos.length === 0}
          skeletonRows={1}
          skeletonHeight="8rem"
          emptyMessage="Nenhum tipo de relatório disponível para o seu perfil."
        >
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
            {tipos.map((tipoDeRelatorio) => (
              <ReportTypeCard
                key={tipoDeRelatorio.tipo}
                tipoDeRelatorio={tipoDeRelatorio}
                gerando={tipoEmGeracao === tipoDeRelatorio.tipo}
                onGerar={gerar}
              />
            ))}
          </div>
        </ListState>
      </div>

      <PanelCard titulo="Gerados recentemente">
        <ListState
          isLoading={isLoadingRelatorios}
          isError={isErrorRelatorios}
          isEmpty={relatorios.length === 0}
          onRetry={() => recarregarRelatorios()}
          skeletonRows={1}
          skeletonHeight="6rem"
          errorMessage="Não foi possível carregar os relatórios gerados."
          emptyMessage="Nenhum relatório gerado ainda."
        >
          <div className="overflow-x-auto">
            <Table columns={colunas} rows={relatorios} getRowKey={(relatorio) => relatorio.id} />
          </div>
        </ListState>
      </PanelCard>
    </div>
  )
}
