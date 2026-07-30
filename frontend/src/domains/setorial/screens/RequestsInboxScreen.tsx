import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Badge, Button, ConfirmDialog, ListState } from '../../../shared/ui'
import type { Solicitacao } from '../domain/setorial.types'
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
  const [solicitacaoEmRecusa, setSolicitacaoEmRecusa] = useState<Solicitacao | null>(null)

  const aprovarTransferencia = useAprovarTransferencia()
  const recusarTransferencia = useRecusarTransferencia()
  const recusarEmprestimo = useRecusarEmprestimo()

  function confirmarRecusa(justificativa: string) {
    if (!solicitacaoEmRecusa) return
    const { id, tipo } = solicitacaoEmRecusa
    const onSuccess = () => setSolicitacaoEmRecusa(null)
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

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={solicitacoes?.length === 0}
        onRetry={() => refetch()}
        skeletonRows={1}
        skeletonHeight="120px"
        errorMessage="Não foi possível carregar as solicitações."
        emptyMessage="Nenhuma solicitação pendente."
      >
        <div className="flex flex-col gap-3">
          {(solicitacoes ?? []).map((solicitacao) => (
            <div key={solicitacao.id} className="rounded-lg border border-slate-200 bg-white p-4">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <Badge tone={solicitacao.tipo === 'TRANSFERENCIA' ? 'neutral' : 'warning'}>
                    {solicitacao.tipo === 'TRANSFERENCIA' ? 'Transferência' : 'Empréstimo'}
                  </Badge>
                  <p className="mt-2 text-sm text-slate-900">{solicitacao.descricao}</p>
                </div>
              </div>

              <div className="mt-3 flex justify-end gap-2">
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
                <Button variant="danger" onClick={() => setSolicitacaoEmRecusa(solicitacao)}>
                  Recusar
                </Button>
              </div>
            </div>
          ))}
        </div>
      </ListState>

      <ConfirmDialog
        isOpen={solicitacaoEmRecusa !== null}
        title={solicitacaoEmRecusa?.tipo === 'EMPRESTIMO' ? 'Recusar empréstimo' : 'Recusar transferência'}
        tone="danger"
        confirmLabel="Recusar solicitação"
        description={
          solicitacaoEmRecusa && (
            <>
              {solicitacaoEmRecusa.descricao}. A justificativa fica registrada e o solicitante é notificado
              (RNF-07).
            </>
          )
        }
        justification={{
          label: 'Justificativa da recusa',
          placeholder: 'Descreva o motivo da recusa',
        }}
        isPending={recusarTransferencia.isPending || recusarEmprestimo.isPending}
        onCancel={() => setSolicitacaoEmRecusa(null)}
        onConfirm={confirmarRecusa}
      />
    </div>
  )
}
