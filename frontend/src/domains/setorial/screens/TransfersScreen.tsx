import { useState } from 'react'
import { useProfile } from '../../../app/providers/ProfileProvider'
import { Badge, Button, ConfirmDialog, ListState, Select } from '../../../shared/ui'
import { PessoaSearchSelect } from '../components/PessoaSearchSelect'
import { STATUS_TRANSFERENCIA_LABEL } from '../domain/setorial.types'
import type { Transferencia } from '../domain/setorial.types'
import { useAprovarTransferencia } from '../hooks/useAprovarTransferencia'
import { useParoquiaOptions } from '../hooks/useParoquiaOptions'
import { useRecusarTransferencia } from '../hooks/useRecusarTransferencia'
import { useSolicitarTransferencia } from '../hooks/useSolicitarTransferencia'
import { useTransferencias } from '../hooks/useTransferencias'

/**
 * Transferências (RF-040/041): mudança definitiva de vínculo de uma pessoa entre paróquias,
 * sempre mediada pelo Setorial (RN-MOVIMENTO-01/RN-17). Quem solicita (pessoa, origem ou
 * destino) não é especificado no protótipo (pendência #9 de `pendencias-gerais.md`) — aqui
 * qualquer perfil de gestão de Pessoas/Paróquia pode solicitar.
 */
export function TransfersScreen() {
  const { perfilAtivo } = useProfile()
  const isSetorial = perfilAtivo === 'SETORIAL'

  const [pessoaId, setPessoaId] = useState('')
  const [pessoaNome, setPessoaNome] = useState('')
  const [paroquiaDestinoId, setParoquiaDestinoId] = useState('')
  const [motivo, setMotivo] = useState('')
  const [transferenciaEmRecusa, setTransferenciaEmRecusa] = useState<Transferencia | null>(null)

  const { data: paroquias } = useParoquiaOptions()
  const { data: transferencias, isLoading, isError, refetch } = useTransferencias()
  const solicitar = useSolicitarTransferencia()
  const aprovar = useAprovarTransferencia()
  const recusar = useRecusarTransferencia()

  const formularioValido = Boolean(pessoaId) && Boolean(paroquiaDestinoId)

  function limparFormulario() {
    setPessoaId('')
    setPessoaNome('')
    setParoquiaDestinoId('')
    setMotivo('')
  }

  function solicitarTransferencia() {
    if (!formularioValido) return
    solicitar.mutate(
      { pessoaId, paroquiaDestinoId, motivo: motivo.trim() || undefined },
      { onSuccess: limparFormulario },
    )
  }

  function confirmarRecusa(justificativa: string) {
    if (!transferenciaEmRecusa) return
    recusar.mutate(
      { transferenciaId: transferenciaEmRecusa.id, justificativa },
      { onSuccess: () => setTransferenciaEmRecusa(null) },
    )
  }

  return (
    <div className="p-6">
      <h1 className="mb-6 text-xl font-semibold text-slate-900">Transferências</h1>

      {!isSetorial && (
        <div className="mb-8 rounded-xl border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-900">Nova solicitação</h2>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <PessoaSearchSelect
              label="Pessoa"
              selectedId={pessoaId}
              selectedLabel={pessoaNome}
              onSelect={(id, nome) => {
                setPessoaId(id)
                setPessoaNome(nome)
              }}
              onClear={() => {
                setPessoaId('')
                setPessoaNome('')
              }}
              placeholder="Buscar pessoa a transferir..."
            />

            <Select
              label="Paróquia de destino"
              value={paroquiaDestinoId}
              placeholder="Selecione a paróquia"
              options={paroquias?.map((paroquia) => ({ value: paroquia.id, label: paroquia.nome })) ?? []}
              onChange={(event) => setParoquiaDestinoId(event.target.value)}
            />

            <div className="sm:col-span-2">
              <label className="flex flex-col gap-1.5">
                <span className="text-sm font-medium text-slate-700">Motivo (opcional)</span>
                <input
                  type="text"
                  value={motivo}
                  onChange={(event) => setMotivo(event.target.value)}
                  className="h-10 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none focus:border-violet-500"
                />
              </label>
            </div>
          </div>

          <div className="mt-4 flex justify-end">
            <Button disabled={!formularioValido || solicitar.isPending} onClick={solicitarTransferencia}>
              Solicitar transferência
            </Button>
          </div>
        </div>
      )}

      <h2 className="mb-3 text-sm font-semibold text-slate-900">Solicitações</h2>

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={transferencias?.content.length === 0}
        onRetry={() => refetch()}
        skeletonRows={1}
        skeletonHeight="120px"
        errorMessage="Não foi possível carregar as transferências."
        emptyMessage="Nenhuma transferência registrada."
      >
        <div className="flex flex-col gap-3">
          {(transferencias?.content ?? []).map((transferencia) => (
            <div key={transferencia.id} className="rounded-lg border border-slate-200 bg-white p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-slate-900">{transferencia.pessoaNome}</p>
                  <p className="text-xs text-slate-500">
                    {transferencia.paroquiaOrigemNome} → {transferencia.paroquiaDestinoNome}
                    {transferencia.motivo && ` · ${transferencia.motivo}`}
                  </p>
                </div>
                <Badge
                  tone={
                    transferencia.status === 'APROVADA'
                      ? 'success'
                      : transferencia.status === 'RECUSADA'
                        ? 'danger'
                        : 'neutral'
                  }
                >
                  {STATUS_TRANSFERENCIA_LABEL[transferencia.status]}
                </Badge>
              </div>

              {isSetorial && transferencia.status === 'PENDENTE' && (
                <div className="mt-3 flex justify-end gap-2">
                  <Button disabled={aprovar.isPending} onClick={() => aprovar.mutate(transferencia.id)}>
                    Aprovar
                  </Button>
                  <Button variant="danger" onClick={() => setTransferenciaEmRecusa(transferencia)}>
                    Recusar
                  </Button>
                </div>
              )}

              {transferencia.justificativaRecusa && (
                <p className="mt-2 text-xs text-rose-600">Recusa: {transferencia.justificativaRecusa}</p>
              )}
            </div>
          ))}
        </div>
      </ListState>

      <ConfirmDialog
        isOpen={transferenciaEmRecusa !== null}
        title="Recusar transferência"
        tone="danger"
        confirmLabel="Recusar transferência"
        description={
          transferenciaEmRecusa && (
            <>
              A transferência de <strong>{transferenciaEmRecusa.pessoaNome}</strong> de{' '}
              {transferenciaEmRecusa.paroquiaOrigemNome} para {transferenciaEmRecusa.paroquiaDestinoNome} será
              recusada. A justificativa fica registrada e a paróquia solicitante é notificada (RNF-07).
            </>
          )
        }
        justification={{
          label: 'Justificativa da recusa',
          placeholder: 'Descreva o motivo da recusa',
        }}
        isPending={recusar.isPending}
        onCancel={() => setTransferenciaEmRecusa(null)}
        onConfirm={confirmarRecusa}
      />
    </div>
  )
}
