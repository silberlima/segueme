import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useProfile } from '../../../app/providers/ProfileProvider'
import { Badge, Button, ConfirmDialog, ListState, Select } from '../../../shared/ui'
import { PessoaSearchSelect } from '../components/PessoaSearchSelect'
import { PAPEL_INTEGRANTE_LABEL } from '../domain/montagem.types'
import type { Convite, PapelIntegrante } from '../domain/montagem.types'
import { useAceitarConvite } from '../hooks/useAceitarConvite'
import { useConvites } from '../hooks/useConvites'
import { useEmitirConvite } from '../hooks/useEmitirConvite'
import { useEquipes } from '../hooks/useEquipes'
import { useRecusarConvite } from '../hooks/useRecusarConvite'

/**
 * Convites (RF-034/035, RN-CONVITE-01): emissão de convite de coordenador/integrante e as ações
 * de aceite/recusa sobre os convites pendentes — mediadas pela Equipe Comando (RN-CONVITE-04, ver
 * limitação de identidade documentada em `docs/pendencias-gerais.md`).
 */
export function InvitesScreen() {
  const { id: encontroId } = useParams<{ id: string }>()
  const { perfilAtivo } = useProfile()
  const podeGerenciar = perfilAtivo === 'EQUIPE_COMANDO'

  const [equipeId, setEquipeId] = useState('')
  const [papel, setPapel] = useState<PapelIntegrante>('COORDENADOR')
  const [pessoaId, setPessoaId] = useState('')
  const [pessoaNome, setPessoaNome] = useState('')
  const [conviteEmRecusa, setConviteEmRecusa] = useState<Convite | null>(null)

  const { data: equipes } = useEquipes(encontroId)
  const { data: convites, isLoading, isError, refetch } = useConvites(encontroId, { status: 'PENDENTE' })
  const emitirConvite = useEmitirConvite()
  const aceitarConvite = useAceitarConvite()
  const recusarConvite = useRecusarConvite()

  const formularioValido = Boolean(encontroId) && equipeId && pessoaId

  function limparFormulario() {
    setEquipeId('')
    setPapel('COORDENADOR')
    setPessoaId('')
    setPessoaNome('')
  }

  function emitir() {
    if (!formularioValido || !encontroId) return
    emitirConvite.mutate({ encontroId, equipeId, pessoaId, papel }, { onSuccess: limparFormulario })
  }

  function confirmarRecusa(justificativa: string) {
    if (!conviteEmRecusa) return
    recusarConvite.mutate(
      { conviteId: conviteEmRecusa.id, justificativa },
      { onSuccess: () => setConviteEmRecusa(null) },
    )
  }

  return (
    <div className="p-6">
      <Link to={`/encontros/${encontroId}/equipes`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Equipes do Encontro
      </Link>

      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-900">Convites</h1>
        <div className="flex items-center gap-3 text-sm font-medium text-violet-600">
          <Link to={`/encontros/${encontroId}/aceites`} className="hover:underline">
            Aceites →
          </Link>
          <Link to={`/encontros/${encontroId}/recusas`} className="hover:underline">
            Recusas →
          </Link>
        </div>
      </div>

      {podeGerenciar && (
        <div className="mb-8 rounded-xl border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-900">Novo convite</h2>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Select
              label="Equipe"
              value={equipeId}
              placeholder="Selecione a equipe"
              options={equipes?.map((equipe) => ({ value: equipe.id, label: equipe.nome })) ?? []}
              onChange={(event) => setEquipeId(event.target.value)}
            />

            <Select
              label="Papel"
              value={papel}
              options={[
                { value: 'COORDENADOR', label: 'Coordenador' },
                { value: 'INTEGRANTE', label: 'Integrante' },
              ]}
              onChange={(event) => setPapel(event.target.value as PapelIntegrante)}
            />

            <div className="sm:col-span-2">
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
                placeholder="Buscar pessoa a convidar..."
              />
            </div>
          </div>

          <div className="mt-4 flex justify-end">
            <Button disabled={!formularioValido || emitirConvite.isPending} onClick={emitir}>
              Emitir convite
            </Button>
          </div>
        </div>
      )}

      <h2 className="mb-3 text-sm font-semibold text-slate-900">Convites pendentes</h2>

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={convites?.length === 0}
        onRetry={() => refetch()}
        skeletonRows={1}
        skeletonHeight="120px"
        errorMessage="Não foi possível carregar os convites."
        emptyMessage="Nenhum convite pendente neste encontro."
      >
        <div className="flex flex-col gap-3">
          {(convites ?? []).map((convite) => (
            <div key={convite.id} className="rounded-lg border border-slate-200 bg-white p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-slate-900">{convite.pessoaNome}</p>
                  <p className="text-xs text-slate-500">
                    {convite.equipeNome} · {PAPEL_INTEGRANTE_LABEL[convite.papel]}
                    {convite.reenvios > 0 && ` · reenviado ${convite.reenvios}x`}
                  </p>
                </div>
                <Badge tone="neutral">Pendente</Badge>
              </div>

              {podeGerenciar && (
                <div className="mt-3 flex justify-end gap-2">
                  <Button disabled={aceitarConvite.isPending} onClick={() => aceitarConvite.mutate(convite.id)}>
                    Aceitar
                  </Button>
                  <Button variant="danger" onClick={() => setConviteEmRecusa(convite)}>
                    Recusar
                  </Button>
                </div>
              )}
            </div>
          ))}
        </div>
      </ListState>

      <ConfirmDialog
        isOpen={conviteEmRecusa !== null}
        title="Recusar convite"
        tone="danger"
        confirmLabel="Recusar convite"
        description={
          conviteEmRecusa && (
            <>
              O convite de <strong>{conviteEmRecusa.pessoaNome}</strong> para {conviteEmRecusa.equipeNome} (
              {PAPEL_INTEGRANTE_LABEL[conviteEmRecusa.papel]}) será recusado. A justificativa fica registrada no
              histórico do convite e não pode ser apagada (RN-CONVITE-02).
            </>
          )
        }
        justification={{
          label: 'Justificativa da recusa',
          placeholder: 'Descreva o motivo da recusa',
        }}
        isPending={recusarConvite.isPending}
        onCancel={() => setConviteEmRecusa(null)}
        onConfirm={confirmarRecusa}
      />
    </div>
  )
}
