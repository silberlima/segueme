import { useEffect } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { useProfile } from '../../../app/providers/ProfileProvider'
import { Badge, Button, Skeleton } from '../../../shared/ui'
import { PAPEL_INTEGRANTE_LABEL, STATUS_INTEGRANTE_LABEL } from '../domain/montagem.types'
import { useEquipe } from '../hooks/useEquipe'
import { useEquipes } from '../hooks/useEquipes'

export function TeamsWorkspaceScreen() {
  const { id: encontroId } = useParams<{ id: string }>()
  const [searchParams, setSearchParams] = useSearchParams()
  const equipeIdSelecionada = searchParams.get('equipeId') ?? undefined
  const { perfilAtivo } = useProfile()
  const podeEditar = perfilAtivo === 'EQUIPE_MONTAGEM'

  const { data: equipes, isLoading, isError, refetch } = useEquipes(encontroId)
  const { data: detalhe, isLoading: detalheCarregando } = useEquipe(equipeIdSelecionada, encontroId)

  useEffect(() => {
    if (!equipeIdSelecionada && equipes && equipes.length > 0) {
      setSearchParams({ equipeId: equipes[0].id }, { replace: true })
    }
  }, [equipeIdSelecionada, equipes, setSearchParams])

  return (
    <div className="p-6">
      <Link to={`/encontros/${encontroId}`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Detalhe do Encontro
      </Link>

      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-900">Equipes do Encontro</h1>
        {podeEditar && (
          <Link
            to={`/encontros/${encontroId}/substituicoes`}
            className="text-sm font-medium text-violet-600 hover:underline"
          >
            Substituições →
          </Link>
        )}
      </div>

      {isLoading && (
        <div className="flex flex-col gap-2">
          <Skeleton variant="rect" height="200px" />
        </div>
      )}

      {isError && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          <p className="mb-3">Não foi possível carregar as equipes deste encontro.</p>
          <Button variant="secondary" onClick={() => refetch()}>
            Tentar novamente
          </Button>
        </div>
      )}

      {equipes && equipes.length === 0 && (
        <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          Nenhum tipo de equipe cadastrado no catálogo nacional.
        </div>
      )}

      {equipes && equipes.length > 0 && (
        <div className="grid grid-cols-1 gap-6 md:grid-cols-[280px_1fr]">
          <div className="flex flex-col gap-2">
            {equipes.map((equipe) => (
              <button
                key={equipe.id}
                onClick={() => setSearchParams({ equipeId: equipe.id })}
                className={`flex items-center justify-between rounded-lg border px-4 py-3 text-left text-sm transition-colors ${
                  equipe.id === equipeIdSelecionada
                    ? 'border-violet-400 bg-violet-50 text-violet-900'
                    : 'border-slate-200 bg-white text-slate-900 hover:border-violet-200'
                }`}
              >
                <span className="font-medium">{equipe.nome}</span>
                <div className="flex items-center gap-2">
                  <span className="text-xs text-slate-500">
                    {equipe.preenchidos}/{equipe.maximo}
                  </span>
                  <Badge tone={equipe.completa ? 'success' : 'warning'}>
                    {equipe.completa ? 'Completa' : 'Incompleta'}
                  </Badge>
                </div>
              </button>
            ))}
          </div>

          <div className="rounded-xl border border-slate-200 bg-white p-6">
            {detalheCarregando && <Skeleton variant="rect" height="200px" />}

            {detalhe && (
              <>
                <div className="mb-4 flex items-center justify-between">
                  <div>
                    <h2 className="text-lg font-semibold text-slate-900">{detalhe.nome}</h2>
                    <p className="text-sm text-slate-500">
                      {detalhe.preenchidos} de {detalhe.maximo} · mínimo {detalhe.minimo}
                      {detalhe.preRequisitoMinEncontros != null &&
                        ` · pré-requisito: ${detalhe.preRequisitoMinEncontros} encontro(s) anterior(es)`}
                    </p>
                    {detalhe.competenciasRequeridas.length > 0 && (
                      <p className="mt-1 text-xs text-slate-400">
                        Competências desejadas: {detalhe.competenciasRequeridas.join(', ')}
                      </p>
                    )}
                    <Link
                      to={`/encontros/${encontroId}/equipes/${detalhe.id}/coordenador`}
                      className="mt-1 inline-block text-xs font-medium text-violet-600 hover:underline"
                    >
                      Painel do Coordenador →
                    </Link>
                  </div>
                  <Badge tone={detalhe.completa ? 'success' : 'warning'}>
                    {detalhe.completa ? 'Completa' : 'Incompleta'}
                  </Badge>
                </div>

                <div className="flex flex-col gap-2">
                  {detalhe.integrantes.map((integrante) => (
                    <div
                      key={integrante.id}
                      className="flex items-center justify-between rounded-lg border border-slate-200 px-4 py-3 text-sm"
                    >
                      <div>
                        <p className="font-medium text-slate-900">{integrante.pessoaNome}</p>
                        <p className="text-xs text-slate-500">{PAPEL_INTEGRANTE_LABEL[integrante.papel]}</p>
                      </div>
                      <Badge
                        tone={
                          integrante.status === 'ACEITO'
                            ? 'success'
                            : integrante.status === 'RECUSADO'
                              ? 'danger'
                              : 'neutral'
                        }
                      >
                        {STATUS_INTEGRANTE_LABEL[integrante.status]}
                      </Badge>
                    </div>
                  ))}

                  {detalhe.vagasAbertas > 0 && podeEditar && (
                    <Link
                      to={`/encontros/${encontroId}/equipes/${detalhe.id}/sugestoes`}
                      className="flex items-center justify-center rounded-lg border-2 border-dashed border-slate-300 px-4 py-3 text-sm font-medium text-slate-500 hover:border-violet-300 hover:text-violet-700"
                    >
                      Vaga em aberto · ver Sugestões Inteligentes →
                    </Link>
                  )}
                  {detalhe.vagasAbertas > 0 && !podeEditar && (
                    <div className="rounded-lg border-2 border-dashed border-slate-300 px-4 py-3 text-center text-sm text-slate-400">
                      {detalhe.vagasAbertas} vaga(s) em aberto
                    </div>
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
