import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useProfile } from '../../../app/providers/ProfileProvider'
import { Badge, Button, Select, Skeleton } from '../../../shared/ui'
import { PessoaSearchSelect } from '../components/PessoaSearchSelect'
import { TIPO_PALESTRANTE_LABEL } from '../domain/conteudo.types'
import type { TipoPalestrante } from '../domain/conteudo.types'
import { useCadastrarPalestrante } from '../hooks/useCadastrarPalestrante'
import { usePalestrantes } from '../hooks/usePalestrantes'

const TIPOS: TipoPalestrante[] = ['EXTERNO', 'SEGUIDOR', 'CLERO']

/**
 * Cadastro de palestrantes: convidado externo, seguidor ou clero (`screen_palestrantes`,
 * RF-052, RN-CONTEUDO-02). Cadastro liberado a Paróquia e Equipe Comando — quem pode editar não
 * é especificado no protótipo (pendência #31 de `pendencias-gerais.md`).
 */
export function SpeakersScreen() {
  const { id: encontroId } = useParams<{ id: string }>()
  const { perfilAtivo } = useProfile()
  const podeCadastrar = perfilAtivo === 'PAROQUIA' || perfilAtivo === 'EQUIPE_COMANDO'

  const [tipo, setTipo] = useState<TipoPalestrante>('EXTERNO')
  const [nome, setNome] = useState('')
  const [pessoaId, setPessoaId] = useState('')
  const [pessoaNome, setPessoaNome] = useState('')

  const { data: palestrantes, isLoading, isError, refetch } = usePalestrantes()
  const cadastrar = useCadastrarPalestrante()

  const formularioValido = tipo === 'SEGUIDOR' ? Boolean(pessoaId) : Boolean(nome.trim())

  function cadastrarPalestrante() {
    if (!formularioValido) return
    cadastrar.mutate(
      { tipo, nome: tipo === 'SEGUIDOR' ? null : nome.trim(), pessoaId: tipo === 'SEGUIDOR' ? pessoaId : null },
      {
        onSuccess: () => {
          setNome('')
          setPessoaId('')
          setPessoaNome('')
        },
      },
    )
  }

  return (
    <div className="p-6">
      <Link to={`/encontros/${encontroId}/palestras`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Palestras
      </Link>
      <h1 className="mb-6 text-xl font-semibold text-slate-900">Palestrantes</h1>

      {podeCadastrar && (
        <div className="mb-8 rounded-xl border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-900">Cadastrar palestrante</h2>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <Select
              label="Tipo"
              value={tipo}
              options={TIPOS.map((item) => ({ value: item, label: TIPO_PALESTRANTE_LABEL[item] }))}
              onChange={(event) => {
                setTipo(event.target.value as TipoPalestrante)
                setNome('')
                setPessoaId('')
                setPessoaNome('')
              }}
            />

            {tipo === 'SEGUIDOR' ? (
              <div className="sm:col-span-2">
                <PessoaSearchSelect
                  label="Pessoa"
                  selectedId={pessoaId}
                  selectedLabel={pessoaNome}
                  onSelect={(id, nomeCompleto) => {
                    setPessoaId(id)
                    setPessoaNome(nomeCompleto)
                  }}
                  onClear={() => {
                    setPessoaId('')
                    setPessoaNome('')
                  }}
                  placeholder="Buscar seguidor por nome..."
                />
              </div>
            ) : (
              <label className="flex flex-col gap-1.5 sm:col-span-2">
                <span className="text-sm font-medium text-slate-700">Nome</span>
                <input
                  type="text"
                  value={nome}
                  onChange={(event) => setNome(event.target.value)}
                  className="h-10 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none focus:border-violet-500"
                />
              </label>
            )}
          </div>

          <div className="mt-4 flex justify-end">
            <Button disabled={!formularioValido || cadastrar.isPending} onClick={cadastrarPalestrante}>
              Cadastrar
            </Button>
          </div>
        </div>
      )}

      {isLoading && <Skeleton variant="rect" height="120px" />}

      {isError && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          <p className="mb-3">Não foi possível carregar os palestrantes.</p>
          <Button variant="secondary" onClick={() => refetch()}>
            Tentar novamente
          </Button>
        </div>
      )}

      {!isLoading && palestrantes && palestrantes.content.length === 0 && (
        <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          Nenhum palestrante cadastrado.
        </div>
      )}

      {palestrantes && palestrantes.content.length > 0 && (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {palestrantes.content.map((palestrante) => (
            <div key={palestrante.id} className="rounded-xl border border-slate-200 bg-white p-4">
              <p className="text-sm font-medium text-slate-900">{palestrante.nome}</p>
              <div className="mt-2">
                <Badge tone="neutral">{TIPO_PALESTRANTE_LABEL[palestrante.tipo]}</Badge>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
