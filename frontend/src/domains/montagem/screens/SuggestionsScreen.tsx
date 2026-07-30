import { Link, useParams } from 'react-router-dom'
import { Badge, Button, ListState } from '../../../shared/ui'
import { useAdicionarIntegrante } from '../hooks/useAdicionarIntegrante'
import { useEquipe } from '../hooks/useEquipe'
import { useSugestoes } from '../hooks/useSugestoes'

/**
 * Sugestões Inteligentes (RF-029/030): tela exclusiva da Equipe Montagem (RN-08/RN-MONTAGEM-02).
 * O score já vem calculado pelo backend — o aviso fixo abaixo reforça RN-10/RN-MONTAGEM-04: a
 * lista nunca substitui a decisão humana da Montagem.
 */
export function SuggestionsScreen() {
  const { id: encontroId, equipeId } = useParams<{ id: string; equipeId: string }>()
  const { data: equipe } = useEquipe(equipeId, encontroId)
  const { data: candidatos, isLoading, isError, refetch } = useSugestoes(encontroId, equipeId)
  const adicionarIntegrante = useAdicionarIntegrante()

  return (
    <div className="flex min-h-screen flex-col p-6">
      <Link
        to={`/encontros/${encontroId}/equipes?equipeId=${equipeId}`}
        className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline"
      >
        ← Equipes do Encontro
      </Link>

      <div className="mb-2 flex items-center gap-3">
        <h1 className="text-xl font-semibold text-slate-900">Sugestões Inteligentes{equipe ? ` · ${equipe.nome}` : ''}</h1>
        <Badge tone="info">Visível só para a Montagem</Badge>
      </div>

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={candidatos?.length === 0}
        onRetry={() => refetch()}
        skeletonHeight="80px"
        errorMessage="Não foi possível carregar as sugestões."
        emptyMessage="Nenhum candidato elegível encontrado para esta equipe."
      >
        <div className="flex flex-1 flex-col gap-3">
          {(candidatos ?? []).map((candidato, index) => (
            <div key={candidato.pessoaId} className="rounded-xl border border-slate-200 bg-white p-4">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <p className="text-xs font-medium text-slate-400">#{index + 1}</p>
                  <p className="text-sm font-semibold text-slate-900">{candidato.pessoaNome}</p>
                  <p className="text-xs text-slate-500">
                    {candidato.situacao} · {candidato.idade} anos · {candidato.paroquiaNome}
                  </p>
                  <div className="mt-2 flex flex-wrap gap-1.5">
                    {candidato.justificativas.map((tag) => (
                      <Badge key={tag} tone={tag === 'Requer empréstimo do setor' ? 'warning' : 'neutral'}>
                        {tag}
                      </Badge>
                    ))}
                  </div>
                </div>

                <div className="flex flex-col items-end gap-2">
                  <div className="flex items-center gap-2">
                    <div className="h-2 w-24 overflow-hidden rounded-full bg-slate-100">
                      <div className="h-full rounded-full bg-violet-500" style={{ width: `${candidato.scoreTotal}%` }} />
                    </div>
                    <span className="text-sm font-semibold text-slate-900">{candidato.scoreTotal}</span>
                  </div>
                  <Button
                    disabled={candidato.requerEmprestimoSetor || !equipeId || !encontroId || adicionarIntegrante.isPending}
                    onClick={() =>
                      equipeId &&
                      encontroId &&
                      adicionarIntegrante.mutate({ equipeId, encontroId, pessoaId: candidato.pessoaId, papel: 'INTEGRANTE' })
                    }
                  >
                    Adicionar à equipe
                  </Button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </ListState>

      <div className="mt-6 rounded-lg border border-violet-200 bg-violet-50 px-4 py-3 text-center text-sm text-violet-800">
        As sugestões nunca substituem a decisão da Montagem.
      </div>
    </div>
  )
}
