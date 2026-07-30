import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Badge, Button, ListState } from '../../../shared/ui'
import { PessoaSearchSelect } from '../components/PessoaSearchSelect'
import { PAPEL_INTEGRANTE_LABEL } from '../domain/montagem.types'
import { useConvites } from '../hooks/useConvites'
import { useEmitirConvite } from '../hooks/useEmitirConvite'
import { useEquipe } from '../hooks/useEquipe'
import { useReenviarConvite } from '../hooks/useReenviarConvite'

function formatarData(dataIso: string): string {
  return new Date(dataIso).toLocaleString('pt-BR')
}

/**
 * Painel do Coordenador (`screen_coordenador`): estatísticas da equipe, lista de convites e ação
 * de convidar/reenviar integrante (RF-035/039). Acesso mediado pela Equipe Comando nesta fase —
 * ver limitação de identidade documentada em `docs/pendencias-gerais.md`. Também cobre o papel de
 * `screen_equipe` (detalhe de equipe a partir de outro contexto), já que o conteúdo é o mesmo.
 */
export function CoordinatorPanelScreen() {
  const { id: encontroId, equipeId } = useParams<{ id: string; equipeId: string }>()
  const [pessoaId, setPessoaId] = useState('')
  const [pessoaNome, setPessoaNome] = useState('')

  const { data: equipe, isLoading: equipeCarregando } = useEquipe(equipeId, encontroId)
  const { data: convites, isLoading, isError, refetch } = useConvites(encontroId, { equipeId })
  const emitirConvite = useEmitirConvite()
  const reenviarConvite = useReenviarConvite()

  function convidarIntegrante() {
    if (!encontroId || !equipeId || !pessoaId) return
    emitirConvite.mutate(
      { encontroId, equipeId, pessoaId, papel: 'INTEGRANTE' },
      {
        onSuccess: () => {
          setPessoaId('')
          setPessoaNome('')
        },
      },
    )
  }

  return (
    <div className="p-6">
      <Link to={`/encontros/${encontroId}/equipes`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Equipes do Encontro
      </Link>

      <ListState isLoading={equipeCarregando} skeletonRows={1} skeletonHeight="80px" />

      {equipe && (
        <div className="mb-6">
          <h1 className="text-xl font-semibold text-slate-900">Painel do Coordenador · {equipe.nome}</h1>
          <p className="text-sm text-slate-500">
            {equipe.preenchidos} de {equipe.maximo} · mínimo {equipe.minimo}
            {equipe.vagasAbertas > 0 && ` · ${equipe.vagasAbertas} vaga(s) em aberto`}
          </p>
        </div>
      )}

      <div className="mb-8 rounded-xl border border-slate-200 bg-white p-6">
        <h2 className="mb-4 text-sm font-semibold text-slate-900">Convidar integrante</h2>
        <div className="flex flex-col gap-4 sm:flex-row sm:items-end">
          <div className="flex-1">
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
          <Button disabled={!pessoaId || emitirConvite.isPending} onClick={convidarIntegrante}>
            Convidar
          </Button>
        </div>
      </div>

      <h2 className="mb-3 text-sm font-semibold text-slate-900">Convites da equipe</h2>

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={convites?.length === 0}
        onRetry={() => refetch()}
        skeletonRows={1}
        skeletonHeight="120px"
        errorMessage="Não foi possível carregar os convites desta equipe."
        emptyMessage="Nenhum convite emitido para esta equipe ainda."
      >
        <div className="flex flex-col gap-2">
          {(convites ?? []).map((convite) => (
            <div key={convite.id} className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-4 text-sm">
              <div>
                <p className="font-medium text-slate-900">{convite.pessoaNome}</p>
                <p className="text-xs text-slate-500">
                  {PAPEL_INTEGRANTE_LABEL[convite.papel]} · enviado em {formatarData(convite.dataEnvioConvite)}
                  {convite.reenvios > 0 && ` · reenviado ${convite.reenvios}x`}
                </p>
              </div>
              <div className="flex items-center gap-2">
                <Badge
                  tone={convite.status === 'ACEITO' ? 'success' : convite.status === 'RECUSADO' ? 'danger' : 'neutral'}
                >
                  {convite.status === 'PENDENTE' ? 'Pendente' : convite.status === 'ACEITO' ? 'Aceito' : 'Recusado'}
                </Badge>
                {convite.status === 'PENDENTE' && (
                  <Button
                    variant="secondary"
                    disabled={reenviarConvite.isPending}
                    onClick={() => reenviarConvite.mutate(convite.id)}
                  >
                    Reenviar
                  </Button>
                )}
              </div>
            </div>
          ))}
        </div>
      </ListState>
    </div>
  )
}
