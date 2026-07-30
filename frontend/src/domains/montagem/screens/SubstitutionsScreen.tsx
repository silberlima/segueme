import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Badge, Button, ConfirmDialog, ListState, Select } from '../../../shared/ui'
import { PessoaSearchSelect } from '../components/PessoaSearchSelect'
import { useEquipe } from '../hooks/useEquipe'
import { useEquipes } from '../hooks/useEquipes'
import { useRegistrarSubstituicao } from '../hooks/useRegistrarSubstituicao'
import { useSubstituicoes } from '../hooks/useSubstituicoes'

function tempoRelativo(dataIso: string): string {
  return new Date(dataIso).toLocaleString('pt-BR')
}

export function SubstitutionsScreen() {
  const { id: encontroId } = useParams<{ id: string }>()
  const { data: equipes } = useEquipes(encontroId)
  const [equipeId, setEquipeId] = useState('')
  const [pessoaSaiId, setPessoaSaiId] = useState('')
  const [motivoSaida, setMotivoSaida] = useState('')
  const [pessoaEntraId, setPessoaEntraId] = useState('')
  const [pessoaEntraNome, setPessoaEntraNome] = useState('')
  const [confirmando, setConfirmando] = useState(false)

  const { data: equipeSelecionada } = useEquipe(equipeId || undefined, encontroId)
  const {
    data: historico,
    isLoading: historicoCarregando,
    isError: historicoComErro,
    refetch: recarregarHistorico,
  } = useSubstituicoes(encontroId)
  const registrarSubstituicao = useRegistrarSubstituicao()

  const formularioValido =
    Boolean(encontroId) && Boolean(equipeId) && Boolean(pessoaSaiId) && Boolean(motivoSaida.trim()) && Boolean(pessoaEntraId)

  const pessoaSaiNome =
    equipeSelecionada?.integrantes.find((integrante) => integrante.pessoaId === pessoaSaiId)?.pessoaNome ?? ''

  function limparFormulario() {
    setEquipeId('')
    setPessoaSaiId('')
    setMotivoSaida('')
    setPessoaEntraId('')
    setPessoaEntraNome('')
  }

  function registrar(justificativa: string) {
    if (!formularioValido || !encontroId) return
    registrarSubstituicao.mutate(
      { encontroId, equipeId, pessoaSaiId, motivoSaida, pessoaEntraId, justificativa },
      {
        onSuccess: () => {
          setConfirmando(false)
          limparFormulario()
        },
      },
    )
  }

  return (
    <div className="p-6">
      <Link to={`/encontros/${encontroId}/equipes`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Equipes do Encontro
      </Link>

      <h1 className="mb-6 text-xl font-semibold text-slate-900">Substituições & Transferências</h1>

      <div className="mb-8 rounded-xl border border-slate-200 bg-white p-6">
        <h2 className="mb-4 text-sm font-semibold text-slate-900">Nova substituição</h2>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Select
            label="Equipe"
            value={equipeId}
            placeholder="Selecione a equipe"
            options={equipes?.map((equipe) => ({ value: equipe.id, label: equipe.nome })) ?? []}
            onChange={(event) => {
              setEquipeId(event.target.value)
              setPessoaSaiId('')
            }}
          />

          <Select
            label="Sai"
            value={pessoaSaiId}
            placeholder="Selecione quem sai"
            disabled={!equipeId}
            options={
              equipeSelecionada?.integrantes
                .filter((integrante) => integrante.status !== 'RECUSADO')
                .map((integrante) => ({ value: integrante.pessoaId, label: integrante.pessoaNome })) ?? []
            }
            onChange={(event) => setPessoaSaiId(event.target.value)}
          />

          <div className="flex flex-col gap-1.5 sm:col-span-2">
            <span className="text-sm font-medium text-slate-700">Motivo da saída</span>
            <input
              type="text"
              value={motivoSaida}
              onChange={(event) => setMotivoSaida(event.target.value)}
              placeholder="Ex.: recusou o convite"
              className="h-10 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none focus:border-violet-500"
            />
          </div>

          <div className="sm:col-span-2">
            <PessoaSearchSelect
              label="Entra"
              selectedId={pessoaEntraId}
              selectedLabel={pessoaEntraNome}
              onSelect={(id, nome) => {
                setPessoaEntraId(id)
                setPessoaEntraNome(nome)
              }}
              onClear={() => {
                setPessoaEntraId('')
                setPessoaEntraNome('')
              }}
              placeholder="Buscar pessoa que vai entrar..."
            />
          </div>

        </div>

        <div className="mt-4 flex justify-end">
          <Button disabled={!formularioValido || registrarSubstituicao.isPending} onClick={() => setConfirmando(true)}>
            Registrar substituição
          </Button>
        </div>
      </div>

      <h2 className="mb-3 text-sm font-semibold text-slate-900">Histórico recente</h2>

      <ListState
        isLoading={historicoCarregando}
        isError={historicoComErro}
        isEmpty={historico?.content.length === 0}
        onRetry={() => recarregarHistorico()}
        skeletonRows={1}
        skeletonHeight="120px"
        errorMessage="Não foi possível carregar o histórico de substituições."
        emptyMessage="Nenhuma substituição registrada neste encontro."
      >
        <div className="flex flex-col gap-2">
          {(historico?.content ?? []).map((item) => (
            <div key={item.id} className="rounded-lg border border-slate-200 bg-white p-4 text-sm">
              <div className="flex items-center justify-between">
                <p className="font-medium text-slate-900">
                  {item.pessoaSaiNome} → {item.pessoaEntraNome}
                </p>
                <Badge tone="success">Concluída</Badge>
              </div>
              <p className="text-xs text-slate-500">
                {item.equipeNome} · {item.motivoSaida} · {tempoRelativo(item.dataSubstituicao)}
              </p>
            </div>
          ))}
        </div>
      </ListState>

      <ConfirmDialog
        isOpen={confirmando}
        title="Registrar substituição"
        tone="danger"
        confirmLabel="Registrar substituição"
        description={
          <>
            <strong>{pessoaSaiNome || 'Quem sai'}</strong> deixa a equipe e{' '}
            <strong>{pessoaEntraNome || 'quem entra'}</strong> assume a vaga. A troca é registrada no histórico das
            duas pessoas, que nunca é apagado, e notifica os responsáveis (RNF-07).
          </>
        }
        justification={{
          label: 'Justificativa da substituição',
          placeholder: 'Descreva o motivo da substituição',
        }}
        isPending={registrarSubstituicao.isPending}
        onCancel={() => setConfirmando(false)}
        onConfirm={registrar}
      />
    </div>
  )
}
