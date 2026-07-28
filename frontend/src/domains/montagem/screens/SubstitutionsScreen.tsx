import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Badge, Button, Select, Skeleton } from '../../../shared/ui'
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
  const [justificativa, setJustificativa] = useState('')

  const { data: equipeSelecionada } = useEquipe(equipeId || undefined, encontroId)
  const { data: historico, isLoading: historicoCarregando } = useSubstituicoes(encontroId)
  const registrarSubstituicao = useRegistrarSubstituicao()

  const formularioValido =
    Boolean(encontroId) && equipeId && pessoaSaiId && motivoSaida.trim() && pessoaEntraId && justificativa.trim();

  function limparFormulario() {
    setEquipeId('')
    setPessoaSaiId('')
    setMotivoSaida('')
    setPessoaEntraId('')
    setPessoaEntraNome('')
    setJustificativa('')
  }

  function registrar() {
    if (!formularioValido || !encontroId) return
    registrarSubstituicao.mutate(
      { encontroId, equipeId, pessoaSaiId, motivoSaida, pessoaEntraId, justificativa },
      { onSuccess: limparFormulario },
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

          <div className="flex flex-col gap-1.5 sm:col-span-2">
            <span className="text-sm font-medium text-slate-700">Justificativa</span>
            <textarea
              value={justificativa}
              onChange={(event) => setJustificativa(event.target.value)}
              rows={3}
              placeholder="Justificativa obrigatória para esta substituição"
              className="rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 outline-none focus:border-violet-500"
            />
          </div>
        </div>

        <div className="mt-4 flex justify-end">
          <Button disabled={!formularioValido || registrarSubstituicao.isPending} onClick={registrar}>
            Registrar substituição
          </Button>
        </div>
      </div>

      <h2 className="mb-3 text-sm font-semibold text-slate-900">Histórico recente</h2>

      {historicoCarregando && <Skeleton variant="rect" height="120px" />}

      {historico && historico.content.length === 0 && (
        <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          Nenhuma substituição registrada neste encontro.
        </div>
      )}

      {historico && historico.content.length > 0 && (
        <div className="flex flex-col gap-2">
          {historico.content.map((item) => (
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
      )}
    </div>
  )
}
