import { Link } from 'react-router-dom'
import { Badge, Button, KpiCard, KpiRow, PageHeader, PanelCard, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { PanelState } from '../components/PanelState'
import { ProgressBar } from '../components/ProgressBar'
import { TeamStatusList } from '../components/TeamStatusList'
import {
  equipesCompletas,
  formatarData,
  formatarMoeda,
  percentualDaMeta,
} from '../domain/dashboard.entity'
import { PENDENCIA_LABEL } from '../domain/dashboard.types'
import type { EncontroDoPainel } from '../domain/dashboard.types'
import { useDashboardParoquia } from '../hooks/useDashboardParoquia'

/**
 * Painel da Paróquia (`dash_paroquia`, RF-053, RN-DASH-01): cartão do Encontro corrente com
 * progresso e checklist de equipes, Encontros recentes, pendências (RN-DASH-02) e financeiro.
 *
 * <p>Paróquia sem Encontro nenhum cai no estado vazio com a ação "Novo encontro", que é o que o
 * protótipo prevê nesse momento.
 */
export function ParishPanelScreen() {
  const { data: painel, isLoading, isError } = useDashboardParoquia()

  const colunasEncontros: TableColumn<EncontroDoPainel>[] = [
    { key: 'numero', header: 'Nº', render: (encontro) => encontro.numero },
    { key: 'padroeiro', header: 'Padroeiro', render: (encontro) => encontro.padroeiro },
    { key: 'inicio', header: 'Início', render: (encontro) => formatarData(encontro.dataInicio) },
    {
      key: 'status',
      header: 'Status',
      render: (encontro) => (
        <Badge tone={encontro.status === 'ATIVO' ? 'info' : 'neutral'}>
          {encontro.status === 'ATIVO' ? 'Em preparação' : 'Encerrado'}
        </Badge>
      ),
    },
    {
      key: 'detalhe',
      header: '',
      render: (encontro) => (
        <Link to={`/encontros/${encontro.id}`} className="text-sm font-medium text-violet-600 hover:underline">
          Abrir
        </Link>
      ),
    },
  ]

  const encontro = painel?.encontroCorrente ?? null

  return (
    <div className="p-6">
      <PageHeader
        titulo="Painel da Paróquia"
        subtitulo={
          encontro
            ? `${encontro.paroquiaNome} · ${encontro.numero}º Encontro (${encontro.padroeiro})`
            : 'Sua paróquia'
        }
        acoes={
          <Link to="/encontros/novo">
            <Button>Novo encontro</Button>
          </Link>
        }
      />

      <PanelState isLoading={isLoading} isError={isError} />

      {painel && (
        <div className="flex flex-col gap-6">
          <KpiRow>
            <KpiCard rotulo="Seguidores ativos" valor={painel.seguidoresAtivos} />
            <KpiCard rotulo="Encontros realizados" valor={painel.encontrosRealizados} />
            <KpiCard
              rotulo="Próximo encontro"
              valor={painel.diasParaProximoEncontro ?? '—'}
              nota={painel.diasParaProximoEncontro === null ? 'Sem encontro futuro em aberto' : 'dias'}
            />
            <KpiCard
              rotulo="Pendências"
              valor={painel.totalPendencias}
              tone={painel.totalPendencias > 0 ? 'atencao' : 'positivo'}
            />
          </KpiRow>

          {!encontro && (
            <div className="rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-500">
              Esta paróquia ainda não criou nenhum Encontro. Comece por “Novo encontro”.
            </div>
          )}

          {encontro && (
            <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
              <PanelCard
                titulo={`${encontro.numero}º Encontro · ${encontro.padroeiro}`}
                acao={
                  <Link
                    to={`/encontros/${encontro.id}`}
                    className="text-sm font-medium text-violet-600 hover:underline"
                  >
                    Abrir encontro
                  </Link>
                }
              >
                <dl className="mb-4 grid grid-cols-2 gap-3 text-sm">
                  <div>
                    <dt className="text-slate-500">Datas</dt>
                    <dd className="text-slate-900">
                      {formatarData(encontro.dataInicio)} — {formatarData(encontro.dataTermino)}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-slate-500">Local</dt>
                    <dd className="text-slate-900">{encontro.local}</dd>
                  </div>
                  <div>
                    <dt className="text-slate-500">Diretor espiritual</dt>
                    <dd className="text-slate-900">{encontro.diretorEspiritualNome}</dd>
                  </div>
                  <div>
                    <dt className="text-slate-500">Situação</dt>
                    <dd className="text-slate-900">
                      {encontro.status === 'ATIVO' ? 'Em preparação' : 'Encerrado'}
                    </dd>
                  </div>
                </dl>

                <div className="flex flex-col gap-3">
                  <ProgressBar
                    rotulo="Equipes montadas"
                    detalhe={`${equipesCompletas(painel.equipes)}/${painel.equipes.length}`}
                    percentual={
                      painel.equipes.length === 0
                        ? 0
                        : Math.round((equipesCompletas(painel.equipes) / painel.equipes.length) * 100)
                    }
                  />
                  <ProgressBar
                    rotulo="Inscrições confirmadas"
                    detalhe={`${painel.inscricoesConfirmadas}/${encontro.vagas}`}
                    percentual={
                      encontro.vagas === 0
                        ? 0
                        : Math.min(100, Math.round((painel.inscricoesConfirmadas / encontro.vagas) * 100))
                    }
                    tone="verde"
                  />
                </div>
              </PanelCard>

              <PanelCard
                titulo="Equipes do encontro"
                acao={
                  <Link
                    to={`/encontros/${encontro.id}/equipes`}
                    className="text-sm font-medium text-violet-600 hover:underline"
                  >
                    Ver todas
                  </Link>
                }
              >
                <TeamStatusList equipes={painel.equipes} />
              </PanelCard>
            </div>
          )}

          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <PanelCard titulo="Pendências">
              {painel.pendencias.length === 0 ? (
                <p className="text-sm text-slate-500">Nenhuma pendência registrada.</p>
              ) : (
                <ul className="flex flex-col gap-2">
                  {painel.pendencias.map((pendencia) => (
                    <li key={pendencia.tipo} className="flex items-center justify-between gap-3 text-sm">
                      <span className="text-slate-600">{PENDENCIA_LABEL[pendencia.tipo]}</span>
                      <Badge tone={pendencia.quantidade > 0 ? 'warning' : 'success'}>
                        {pendencia.quantidade}
                      </Badge>
                    </li>
                  ))}
                </ul>
              )}
            </PanelCard>

            <PanelCard
              titulo="Financeiro do encontro"
              acao={
                encontro && (
                  <Link
                    to={`/encontros/${encontro.id}/financeiro`}
                    className="text-sm font-medium text-violet-600 hover:underline"
                  >
                    Abrir financeiro
                  </Link>
                )
              }
            >
              <p className="text-2xl font-semibold text-slate-900">{formatarMoeda(painel.financeiro.saldo)}</p>
              <p className="mb-4 text-xs text-slate-500">
                Receitas {formatarMoeda(painel.financeiro.totalReceitas)} · Despesas{' '}
                {formatarMoeda(painel.financeiro.totalDespesas)}
              </p>

              {painel.campanhas.length === 0 ? (
                <p className="text-sm text-slate-500">Nenhuma campanha de arrecadação.</p>
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
          </div>

          <PanelCard
            titulo="Encontros recentes"
            acao={
              <Link to="/encontros" className="text-sm font-medium text-violet-600 hover:underline">
                Ver todos
              </Link>
            }
          >
            {painel.encontrosRecentes.length === 0 ? (
              <p className="text-sm text-slate-500">Nenhum Encontro registrado.</p>
            ) : (
              <div className="overflow-x-auto">
                <Table
                  columns={colunasEncontros}
                  rows={painel.encontrosRecentes}
                  getRowKey={(item) => item.id}
                />
              </div>
            )}
          </PanelCard>
        </div>
      )}
    </div>
  )
}
