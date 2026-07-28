import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Badge, Button, Skeleton } from '../../../shared/ui'
import { useAprovarTransferencia } from '../hooks/useAprovarTransferencia'
import { useRecusarEmprestimo } from '../hooks/useRecusarEmprestimo'
import { useRecusarTransferencia } from '../hooks/useRecusarTransferencia'
import { useSolicitacoes } from '../hooks/useSolicitacoes'

/**
 * Solicitações (RF-046): inbox único do Setorial, combinando Transferências e Empréstimos
 * pendentes (`transferencias-e-emprestimos.md` 2.3). Não é um agregado próprio — as ações usam
 * diretamente os endpoints de Transferências/Empréstimos. Para Empréstimo, "Autorizar" leva à
 * tela dedicada de Empréstimos, já que selecionar candidatos é um fluxo de múltiplas etapas
 * (filtro + seleção múltipla, RN-MOVIMENTO-05) que não cabe num único clique no card.
 */
export function RequestsInboxScreen() {
  const { data: solicitacoes, isLoading, isError, refetch } = useSolicitacoes()
  const [justificativaPorId, setJustificativaPorId] = useState<Record<string, string>>({})

  const aprovarTransferencia = useAprovarTransferencia()
  const recusarTransferencia = useRecusarTransferencia()
  const recusarEmprestimo = useRecusarEmprestimo()

  function recusar(id: string, tipo: 'TRANSFERENCIA' | 'EMPRESTIMO') {
    const justificativa = (justificativaPorId[id] ?? '').trim()
    if (!justificativa) return
    const onSuccess = () => setJustificativaPorId((atual) => ({ ...atual, [id]: '' }))
    if (tipo === 'TRANSFERENCIA') {
      recusarTransferencia.mutate({ transferenciaId: id, justificativa }, { onSuccess })
    } else {
      recusarEmprestimo.mutate({ emprestimoId: id, justificativa }, { onSuccess })
    }
  }

  return (
    <div className="p-6">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-900">Solicitações</h1>
        <Badge tone="info">{solicitacoes?.length ?? 0} pendente(s)</Badge>
      </div>

      {isLoading && <Skeleton variant="rect" height="120px" />}

      {isError && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          <p className="mb-3">Não foi possível carregar as solicitações.</p>
          <Button variant="secondary" onClick={() => refetch()}>
            Tentar novamente
          </Button>
        </div>
      )}

      {solicitacoes && solicitacoes.length === 0 && (
        <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          Nenhuma solicitação pendente.
        </div>
      )}

      {solicitacoes && solicitacoes.length > 0 && (
        <div className="flex flex-col gap-3">
          {solicitacoes.map((solicitacao) => (
            <div key={solicitacao.id} className="rounded-lg border border-slate-200 bg-white p-4">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <Badge tone={solicitacao.tipo === 'TRANSFERENCIA' ? 'neutral' : 'warning'}>
                    {solicitacao.tipo === 'TRANSFERENCIA' ? 'Transferência' : 'Empréstimo'}
                  </Badge>
                  <p className="mt-2 text-sm text-slate-900">{solicitacao.descricao}</p>
                </div>
              </div>

              <div className="mt-3 flex flex-col gap-2 sm:flex-row sm:items-center">
                <input
                  type="text"
                  value={justificativaPorId[solicitacao.id] ?? ''}
                  onChange={(event) =>
                    setJustificativaPorId((atual) => ({ ...atual, [solicitacao.id]: event.target.value }))
                  }
                  placeholder="Justificativa (obrigatória para recusar)"
                  className="h-9 flex-1 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none focus:border-violet-500"
                />
                <div className="flex gap-2">
                  {solicitacao.tipo === 'TRANSFERENCIA' ? (
                    <Button
                      disabled={aprovarTransferencia.isPending}
                      onClick={() => aprovarTransferencia.mutate(solicitacao.id)}
                    >
                      Aprovar
                    </Button>
                  ) : (
                    <Link to="/setorial/emprestimos">
                      <Button>Autorizar</Button>
                    </Link>
                  )}
                  <Button
                    variant="danger"
                    disabled={
                      !(justificativaPorId[solicitacao.id] ?? '').trim() ||
                      recusarTransferencia.isPending ||
                      recusarEmprestimo.isPending
                    }
                    onClick={() => recusar(solicitacao.id, solicitacao.tipo)}
                  >
                    Recusar
                  </Button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
