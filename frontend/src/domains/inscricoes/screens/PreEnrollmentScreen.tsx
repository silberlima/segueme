import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { Button, ListState, Modal, Table, Tabs } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import type { TabItem } from '../../../shared/ui/Tabs/Tabs.types'
import { PessoaSearchSelect } from '../components/PessoaSearchSelect'
import { OcupacaoVagasBar } from '../components/OcupacaoVagasBar'
import { STATUS_PRE_INSCRICAO_LABEL } from '../domain/inscricao.types'
import type { PreInscricao, StatusPreInscricao } from '../domain/inscricao.types'
import { useCriarPreInscricao } from '../hooks/useCriarPreInscricao'
import { useEncontroContexto } from '../hooks/useEncontroContexto'
import { useFunil } from '../hooks/useFunil'
import { useMarcarEmAnalise } from '../hooks/useMarcarEmAnalise'
import { usePreInscricoes } from '../hooks/usePreInscricoes'

const TABS: TabItem[] = [
  { id: 'AGUARDANDO_FICHA', label: 'Aguardando ficha' },
  { id: 'EM_ANALISE', label: 'Em análise' },
  { id: 'CONFIRMADA', label: 'Confirmadas' },
]

export function PreEnrollmentScreen() {
  const { id: encontroId } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState<StatusPreInscricao>('AGUARDANDO_FICHA')
  const [page, setPage] = useState(0)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [pessoaId, setPessoaId] = useState('')
  const [pessoaNome, setPessoaNome] = useState('')
  const [feedback, setFeedback] = useState<string | null>(null)

  const { data: encontro } = useEncontroContexto(encontroId)
  const { data: funil } = useFunil(encontroId)
  const { data, isLoading, isError, refetch } = usePreInscricoes(
    { encontroId, status: activeTab },
    page,
  )
  const criarPreInscricao = useCriarPreInscricao()
  const marcarEmAnalise = useMarcarEmAnalise()

  const ocupadas = funil ? funil.preInscritos + funil.emAnalise + funil.confirmados : 0

  const columns: TableColumn<PreInscricao>[] = [
    {
      key: 'candidato',
      header: 'Candidato',
      render: (candidato) => (
        <div>
          <span className="block font-medium text-slate-900">{candidato.pessoaNome}</span>
          <span className="block text-xs text-slate-500">{candidato.idade} anos</span>
        </div>
      ),
    },
    { key: 'indicadoPor', header: 'Indicado por', render: (candidato) => candidato.indicadoPorNome ?? '—' },
    {
      key: 'recebida',
      header: 'Recebida',
      render: (candidato) => new Date(candidato.recebidaEm).toLocaleDateString('pt-BR'),
    },
    {
      key: 'acao',
      header: 'Ação',
      render: (candidato) => (
        <div className="flex gap-2">
          {candidato.status === 'AGUARDANDO_FICHA' && (
            <Button
              variant="secondary"
              onClick={() => marcarEmAnalise.mutate(candidato.id)}
              disabled={marcarEmAnalise.isPending}
            >
              Marcar em análise
            </Button>
          )}
          {candidato.status !== 'CONFIRMADA' && (
            <Button onClick={() => navigate(`/encontros/${encontroId}/inscricoes/${candidato.id}/ficha`)}>
              Preencher ficha
            </Button>
          )}
        </div>
      ),
    },
  ]

  async function confirmarNovaPreInscricao() {
    if (!encontroId || !pessoaId) {
      return
    }
    const resultado = await criarPreInscricao.mutateAsync({ pessoaId, encontroId })
    setFeedback(
      resultado.emReserva
        ? `${pessoaNome} entrou no Cadastro de Reserva: as vagas deste encontro estão esgotadas.`
        : `${pessoaNome} foi pré-inscrito(a) com sucesso.`,
    )
    setIsModalOpen(false)
    setPessoaId('')
    setPessoaNome('')
  }

  return (
    <div className="p-6">
      <Link to={`/encontros/${encontroId}`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Detalhe do Encontro
      </Link>

      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="mb-1 text-xl font-semibold text-slate-900">Pré-inscrições</h1>
          {encontro && (
            <p className="text-sm text-slate-500">
              #{encontro.numero} · {encontro.padroeiro} — a ficha nacional é preenchida pela Equipe Fichas.
            </p>
          )}
        </div>
        <Button onClick={() => setIsModalOpen(true)}>Nova pré-inscrição</Button>
      </div>

      {feedback && (
        <div className="mb-4 rounded-xl border border-violet-200 bg-violet-50 p-3 text-sm text-violet-700">
          {feedback}
        </div>
      )}

      {encontro && funil && (
        <div className="mb-6">
          <OcupacaoVagasBar ocupadas={ocupadas} vagas={encontro.vagas} emReserva={funil.emReserva} />
        </div>
      )}

      <Tabs
        tabs={TABS.map((tab) => ({
          ...tab,
          label: `${tab.label}${funil ? ` (${countFor(tab.id as StatusPreInscricao, funil)})` : ''}`,
        }))}
        activeTabId={activeTab}
        onChange={(tabId) => {
          setActiveTab(tabId as StatusPreInscricao)
          setPage(0)
        }}
      />

      <div className="mt-4">
        <ListState
          isLoading={isLoading}
          isError={isError}
          isEmpty={data?.content.length === 0}
          onRetry={() => refetch()}
          skeletonRows={4}
          errorMessage="Não foi possível carregar as pré-inscrições. Tente novamente mais tarde."
          emptyMessage={`Nenhum candidato em ${STATUS_PRE_INSCRICAO_LABEL[activeTab].toLowerCase()}.`}
        >
          <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
            <Table columns={columns} rows={data?.content ?? []} getRowKey={(candidato) => candidato.id} />
          </div>
          <div className="mt-4 flex items-center justify-between text-sm text-slate-500">
            <span>
              Página {(data?.page ?? 0) + 1} de {Math.max(data?.totalPages ?? 1, 1)} · {data?.totalElements ?? 0}{' '}
              candidatos
            </span>
            <div className="flex gap-2">
              <Button variant="secondary" disabled={page === 0} onClick={() => setPage((current) => current - 1)}>
                Anterior
              </Button>
              <Button
                variant="secondary"
                disabled={(data?.page ?? 0) + 1 >= (data?.totalPages ?? 0)}
                onClick={() => setPage((current) => current + 1)}
              >
                Próxima
              </Button>
            </div>
          </div>
        </ListState>
      </div>

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Nova pré-inscrição">
        <div className="flex flex-col gap-4">
          <p className="text-sm text-slate-500">
            Busque a pessoa no cadastro nacional. Se ela ainda não existe, cadastre-a primeiro em Pessoas.
          </p>
          <PessoaSearchSelect
            label="Candidato"
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
          />
          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancelar
            </Button>
            <Button
              onClick={confirmarNovaPreInscricao}
              disabled={!pessoaId || criarPreInscricao.isPending}
            >
              Pré-inscrever
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  )
}

function countFor(status: StatusPreInscricao, funil: { preInscritos: number; emAnalise: number; confirmados: number }) {
  if (status === 'AGUARDANDO_FICHA') return funil.preInscritos
  if (status === 'EM_ANALISE') return funil.emAnalise
  return funil.confirmados
}
