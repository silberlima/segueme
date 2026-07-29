import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useProfile } from '../../../app/providers/ProfileProvider'
import { Badge, Button, Skeleton } from '../../../shared/ui'
import { CampanhaProgressBar } from '../components/CampanhaProgressBar'
import { STATUS_CAMPANHA_LABEL } from '../domain/financeiro.types'
import { useCampanhas } from '../hooks/useCampanhas'
import { useCriarCampanha } from '../hooks/useCriarCampanha'
import { useEncerrarCampanha } from '../hooks/useEncerrarCampanha'
import { useRegistrarArrecadacao } from '../hooks/useRegistrarArrecadacao'

/** Campanhas de arrecadação (`screen_campanhas`, RF-050, RN-FINANCEIRO-03). Gestão restrita à Equipe Finanças. */
export function CampaignsScreen() {
  const { id: encontroId } = useParams<{ id: string }>()
  const { perfilAtivo } = useProfile()
  const isEquipeFinancas = perfilAtivo === 'EQUIPE_FINANCAS'

  const [nome, setNome] = useState('')
  const [meta, setMeta] = useState('')
  const [arrecadacaoPorId, setArrecadacaoPorId] = useState<Record<string, string>>({})

  const { data: campanhas, isLoading, isError, refetch } = useCampanhas(encontroId ?? '')
  const criar = useCriarCampanha()
  const registrarArrecadacao = useRegistrarArrecadacao()
  const encerrar = useEncerrarCampanha()

  const formularioValido = Boolean(nome.trim()) && Number(meta) > 0

  function criarCampanha() {
    if (!encontroId || !formularioValido) return
    criar.mutate(
      { encontroId, nome: nome.trim(), meta: Number(meta) },
      { onSuccess: () => { setNome(''); setMeta('') } },
    )
  }

  function registrarArrecadacaoDaCampanha(campanhaId: string) {
    const valor = Number(arrecadacaoPorId[campanhaId] ?? '')
    if (!(valor > 0)) return
    registrarArrecadacao.mutate(
      { campanhaId, valor },
      { onSuccess: () => setArrecadacaoPorId((atual) => ({ ...atual, [campanhaId]: '' })) },
    )
  }

  return (
    <div className="p-6">
      <Link to={`/encontros/${encontroId}/financeiro`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Financeiro
      </Link>
      <h1 className="mb-6 text-xl font-semibold text-slate-900">Campanhas</h1>

      {isEquipeFinancas && (
        <div className="mb-8 rounded-xl border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-900">Nova campanha</h2>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <label className="flex flex-col gap-1.5">
              <span className="text-sm font-medium text-slate-700">Nome</span>
              <input
                type="text"
                value={nome}
                onChange={(event) => setNome(event.target.value)}
                className="h-10 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none focus:border-violet-500"
              />
            </label>

            <label className="flex flex-col gap-1.5">
              <span className="text-sm font-medium text-slate-700">Meta</span>
              <input
                type="number"
                min="0"
                step="0.01"
                value={meta}
                onChange={(event) => setMeta(event.target.value)}
                className="h-10 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none focus:border-violet-500"
              />
            </label>
          </div>

          <div className="mt-4 flex justify-end">
            <Button disabled={!formularioValido || criar.isPending} onClick={criarCampanha}>
              Criar campanha
            </Button>
          </div>
        </div>
      )}

      {isLoading && <Skeleton variant="rect" height="120px" />}

      {isError && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          <p className="mb-3">Não foi possível carregar as campanhas.</p>
          <Button variant="secondary" onClick={() => refetch()}>
            Tentar novamente
          </Button>
        </div>
      )}

      {campanhas && campanhas.content.length === 0 && (
        <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          Nenhuma campanha registrada.
        </div>
      )}

      {campanhas && campanhas.content.length > 0 && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {campanhas.content.map((campanha) => (
            <div key={campanha.id} className="rounded-xl border border-slate-200 bg-white p-4">
              <div className="mb-3 flex items-center justify-between">
                <p className="text-sm font-medium text-slate-900">{campanha.nome}</p>
                <Badge tone={campanha.status === 'ATIVA' ? 'info' : 'neutral'}>
                  {STATUS_CAMPANHA_LABEL[campanha.status]}
                </Badge>
              </div>

              <CampanhaProgressBar valorArrecadado={campanha.valorArrecadado} meta={campanha.meta} />

              {isEquipeFinancas && campanha.status === 'ATIVA' && (
                <div className="mt-3 flex flex-col gap-2 sm:flex-row">
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    placeholder="Valor arrecadado"
                    value={arrecadacaoPorId[campanha.id] ?? ''}
                    onChange={(event) =>
                      setArrecadacaoPorId((atual) => ({ ...atual, [campanha.id]: event.target.value }))
                    }
                    className="h-9 flex-1 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none focus:border-violet-500"
                  />
                  <div className="flex gap-2">
                    <Button
                      disabled={!(Number(arrecadacaoPorId[campanha.id] ?? '') > 0) || registrarArrecadacao.isPending}
                      onClick={() => registrarArrecadacaoDaCampanha(campanha.id)}
                    >
                      Registrar
                    </Button>
                    <Button variant="secondary" disabled={encerrar.isPending} onClick={() => encerrar.mutate(campanha.id)}>
                      Encerrar
                    </Button>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
