import { Link } from 'react-router-dom'
import { Badge, KpiCard, KpiRow, ListState, PageHeader, PanelCard } from '../../../shared/ui'
import { ProgressBar } from '../components/ProgressBar'
import { TeamStatusList } from '../components/TeamStatusList'
import { useDashboardMontagem } from '../hooks/useDashboardMontagem'

/**
 * Painel de Montagem (`dash_montagem`, RF-053, RN-DASH-01). As sugestões inteligentes são exclusivas
 * deste perfil (RN-08/RN-MONTAGEM-02) e nunca substituem a decisão da Montagem (RN-10): "Adicionar à
 * equipe" leva à tela de sugestões da equipe, onde a inclusão é confirmada.
 */
export function TeamAssemblyPanelScreen() {
  const { data: painel, isLoading, isError, refetch } = useDashboardMontagem()
  const encontro = painel?.encontroCorrente ?? null
  const prioritaria = painel?.equipePrioritaria ?? null

  return (
    <div className="p-6">
      <PageHeader
        titulo="Painel de Montagem"
        subtitulo={
          encontro
            ? `${encontro.numero}º Encontro (${encontro.padroeiro}) · ${encontro.paroquiaNome}`
            : 'Sem Encontro em preparação'
        }
        acoes={
          encontro && (
            <Link
              to={`/encontros/${encontro.id}/substituicoes`}
              className="text-sm font-medium text-violet-600 hover:underline"
            >
              Substituição
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
              rotulo="Equipes incompletas"
              valor={painel.equipesIncompletas}
              tone={painel.equipesIncompletas > 0 ? 'atencao' : 'positivo'}
            />
            <KpiCard rotulo="Vagas abertas" valor={painel.vagasAbertas} />
            <KpiCard rotulo="Sugestões disponíveis" valor={painel.sugestoesDisponiveis} />
            <KpiCard rotulo="Recusas" valor={painel.recusas} tone={painel.recusas > 0 ? 'critico' : 'neutro'} />
          </KpiRow>

          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <PanelCard
              titulo="Equipes do encontro"
              acao={
                encontro && (
                  <Link
                    to={`/encontros/${encontro.id}/equipes`}
                    className="text-sm font-medium text-violet-600 hover:underline"
                  >
                    Ver todas
                  </Link>
                )
              }
            >
              <TeamStatusList equipes={painel.equipes} />
            </PanelCard>

            <PanelCard
              titulo={prioritaria ? `Sugestões para ${prioritaria.nome}` : 'Sugestões inteligentes'}
              acao={
                encontro &&
                prioritaria && (
                  <Link
                    to={`/encontros/${encontro.id}/equipes/${prioritaria.id}/sugestoes`}
                    className="text-sm font-medium text-violet-600 hover:underline"
                  >
                    Ver todas
                  </Link>
                )
              }
            >
              {!prioritaria && (
                <p className="text-sm text-slate-500">
                  Nenhuma equipe com vaga aberta — não há sugestão a fazer neste momento.
                </p>
              )}

              {prioritaria && (
                <div className="mb-4">
                  <ProgressBar
                    rotulo="Vagas da equipe prioritária"
                    detalhe={`${prioritaria.preenchidos}/${prioritaria.maximo}`}
                    percentual={Math.round((prioritaria.preenchidos / Math.max(1, prioritaria.maximo)) * 100)}
                    tone="ambar"
                  />
                </div>
              )}

              {prioritaria && painel.sugestoes.length === 0 && (
                <p className="text-sm text-slate-500">Nenhum candidato elegível para esta equipe.</p>
              )}

              <ul className="flex flex-col gap-3">
                {painel.sugestoes.map((candidato) => (
                  <li key={candidato.pessoaId} className="rounded-lg border border-slate-200 p-3">
                    <div className="mb-2 flex items-center justify-between gap-2">
                      <div>
                        <p className="text-sm font-medium text-slate-900">{candidato.pessoaNome}</p>
                        <p className="text-xs text-slate-500">
                          {candidato.idade} anos · {candidato.paroquiaNome}
                        </p>
                      </div>
                      <span className="text-sm font-semibold text-violet-700">{candidato.scoreTotal}%</span>
                    </div>

                    <div className="mb-2 h-2 w-full overflow-hidden rounded-full bg-slate-100">
                      <div
                        className="h-full rounded-full bg-violet-500 transition-all"
                        style={{ width: `${candidato.scoreTotal}%` }}
                      />
                    </div>

                    <div className="flex flex-wrap items-center gap-1.5">
                      {candidato.requerEmprestimoSetor && <Badge tone="warning">Requer empréstimo</Badge>}
                      {candidato.justificativas.map((justificativa) => (
                        <Badge key={justificativa}>{justificativa}</Badge>
                      ))}
                    </div>

                    {encontro && prioritaria && (
                      <div className="mt-2 flex gap-3">
                        <Link
                          to={`/encontros/${encontro.id}/equipes/${prioritaria.id}/sugestoes`}
                          className="text-sm font-medium text-violet-600 hover:underline"
                        >
                          Adicionar à equipe
                        </Link>
                        <Link
                          to={`/pessoas/${candidato.pessoaId}`}
                          className="text-sm font-medium text-slate-600 hover:underline"
                        >
                          Perfil
                        </Link>
                      </div>
                    )}
                  </li>
                ))}
              </ul>
            </PanelCard>
          </div>
        </div>
      )}
    </div>
  )
}
