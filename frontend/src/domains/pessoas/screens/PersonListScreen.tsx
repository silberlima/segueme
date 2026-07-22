import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Badge, Button, Input, Select, Skeleton, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { PersonAvatar } from '../components/PersonAvatar'
import { calcularIdade } from '../domain/pessoa.entity'
import { ESTADO_CIVIL_LABEL, SITUACAO_LABEL } from '../domain/pessoa.types'
import type { PessoaResumo, SituacaoPessoa } from '../domain/pessoa.types'
import { useCompetencias } from '../hooks/useCompetencias'
import { usePessoas } from '../hooks/usePessoas'

const SITUACAO_TONE: Record<SituacaoPessoa, 'neutral' | 'info' | 'success'> = {
  PRE_INSCRITO: 'neutral',
  INSCRITO: 'info',
  SEGUIMISTA: 'info',
  SEGUIDOR: 'success',
}

const PAGE_SIZE = 20

export function PersonListScreen() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()

  const [nome, setNome] = useState('')
  const [competenciaId, setCompetenciaId] = useState(searchParams.get('competenciaId') ?? '')
  const [page, setPage] = useState(0)

  const { data: competencias } = useCompetencias()
  const { data, isLoading, isError, refetch } = usePessoas(
    { nome: nome || undefined, competenciaId: competenciaId || undefined },
    page,
    PAGE_SIZE,
  )

  const columns: TableColumn<PessoaResumo>[] = [
    {
      key: 'pessoa',
      header: 'Pessoa',
      render: (pessoa) => (
        <button
          type="button"
          onClick={() => navigate(`/pessoas/${pessoa.id}`)}
          className="flex items-center gap-3 text-left hover:underline"
        >
          <PersonAvatar nomeCompleto={pessoa.nomeCompleto} />
          <span>
            <span className="block font-medium text-slate-900">{pessoa.nomeCompleto}</span>
            <span className="block text-xs text-slate-500">
              {calcularIdade(pessoa.dataNascimento)} anos · {ESTADO_CIVIL_LABEL[pessoa.estadoCivil]}
            </span>
          </span>
        </button>
      ),
    },
    { key: 'paroquia', header: 'Paróquia atual', render: (pessoa) => pessoa.paroquiaNome },
    {
      key: 'situacao',
      header: 'Situação',
      render: (pessoa) => <Badge tone={SITUACAO_TONE[pessoa.situacao]}>{SITUACAO_LABEL[pessoa.situacao]}</Badge>,
    },
    {
      key: 'competencias',
      header: 'Competências',
      render: (pessoa) => (
        <div className="flex flex-wrap gap-1">
          {pessoa.competencias.length === 0 && <span className="text-slate-400">—</span>}
          {pessoa.competencias.map((competencia) => (
            <Badge key={competencia} tone="neutral">
              {competencia}
            </Badge>
          ))}
        </div>
      ),
    },
    { key: 'encontros', header: 'Encontros', render: (pessoa) => pessoa.numEncontros },
  ]

  return (
    <div className="p-6">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="mb-1 text-xl font-semibold text-slate-900">Pessoas</h1>
          <p className="text-sm text-slate-500">Cadastro nacional único de pessoas do movimento.</p>
        </div>
        <Button onClick={() => navigate('/pessoas/novo')}>Nova pessoa</Button>
      </div>

      <div className="mb-4 flex flex-wrap items-end gap-3">
        <div className="w-64">
          <Input
            label="Buscar por nome"
            value={nome}
            placeholder="Nome da pessoa"
            onChange={(event) => {
              setPage(0)
              setNome(event.target.value)
            }}
          />
        </div>
        <div className="w-56">
          <Select
            label="Competência"
            value={competenciaId}
            options={[
              { value: '', label: 'Todas as competências' },
              ...(competencias ?? []).map((competencia) => ({ value: competencia.id, label: competencia.nome })),
            ]}
            onChange={(event) => {
              setPage(0)
              setCompetenciaId(event.target.value)
            }}
          />
        </div>
      </div>

      {isLoading && (
        <div className="flex flex-col gap-2">
          {Array.from({ length: 5 }).map((_, index) => (
            <Skeleton key={index} variant="rect" height="52px" />
          ))}
        </div>
      )}

      {isError && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          <p className="mb-3">Não foi possível carregar as pessoas. Tente novamente mais tarde.</p>
          <Button variant="secondary" onClick={() => refetch()}>
            Tentar novamente
          </Button>
        </div>
      )}

      {data && data.content.length === 0 && (
        <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          <p className="mb-3">Nenhuma pessoa encontrada para os filtros aplicados.</p>
          <div className="flex gap-2">
            <Button
              variant="secondary"
              onClick={() => {
                setNome('')
                setCompetenciaId('')
                setPage(0)
              }}
            >
              Limpar filtros
            </Button>
            <Button onClick={() => navigate('/pessoas/novo')}>Nova pessoa</Button>
          </div>
        </div>
      )}

      {data && data.content.length > 0 && (
        <>
          <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
            <Table columns={columns} rows={data.content} getRowKey={(pessoa) => pessoa.id} />
          </div>
          <div className="mt-4 flex items-center justify-between text-sm text-slate-500">
            <span>
              Página {data.page + 1} de {Math.max(data.totalPages, 1)} · {data.totalElements} pessoas
            </span>
            <div className="flex gap-2">
              <Button variant="secondary" disabled={page === 0} onClick={() => setPage((current) => current - 1)}>
                Anterior
              </Button>
              <Button
                variant="secondary"
                disabled={data.page + 1 >= data.totalPages}
                onClick={() => setPage((current) => current + 1)}
              >
                Próxima
              </Button>
            </div>
          </div>
        </>
      )}
    </div>
  )
}
