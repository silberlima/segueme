import { Badge, Button, Input, Modal, Select, Skeleton, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { useDisclosure } from '../../../shared/hooks/useDisclosure'
import type { Casal, PessoaResumo, SituacaoPessoa } from '../domain/pessoa.types'
import { SITUACAO_LABEL } from '../domain/pessoa.types'
import { useCasais } from '../hooks/useCasais'
import { useCoupleForm } from '../hooks/useCoupleForm'
import { usePessoas } from '../hooks/usePessoas'

const SITUACAO_TONE: Record<SituacaoPessoa, 'neutral' | 'info' | 'success'> = {
  PRE_INSCRITO: 'neutral',
  INSCRITO: 'info',
  SEGUIMISTA: 'info',
  SEGUIDOR: 'success',
}

function PessoaResumoCelula({ pessoa }: { pessoa: PessoaResumo }) {
  return (
    <div>
      <p className="font-medium text-slate-900">{pessoa.nomeCompleto}</p>
      <p className="text-xs text-slate-500">
        {pessoa.paroquiaNome} · <Badge tone={SITUACAO_TONE[pessoa.situacao]}>{SITUACAO_LABEL[pessoa.situacao]}</Badge>
      </p>
    </div>
  )
}

export function CouplesScreen() {
  const { data, isLoading, isError, refetch } = useCasais(0, 50)
  const { data: pessoasDisponiveis } = usePessoas({}, 0, 100)
  const modal = useDisclosure()
  const form = useCoupleForm(() => {
    modal.close()
    void refetch()
  })

  const columns: TableColumn<Casal>[] = [
    { key: 'pessoa1', header: 'Pessoa 1', render: (casal) => <PessoaResumoCelula pessoa={casal.pessoa1} /> },
    { key: 'pessoa2', header: 'Pessoa 2', render: (casal) => <PessoaResumoCelula pessoa={casal.pessoa2} /> },
    {
      key: 'dataVinculo',
      header: 'Vínculo desde',
      render: (casal) => new Date(casal.dataVinculo).toLocaleDateString('pt-BR'),
    },
  ]

  return (
    <div className="p-6">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="mb-1 text-xl font-semibold text-slate-900">Casais</h1>
          <p className="text-sm text-slate-500">Vínculos de casal entre pessoas do cadastro nacional (RN-03).</p>
        </div>
        <Button
          onClick={() => {
            form.reset()
            modal.open()
          }}
        >
          Vincular casal
        </Button>
      </div>

      {isLoading && (
        <div className="flex flex-col gap-2">
          <Skeleton variant="rect" height="52px" />
          <Skeleton variant="rect" height="52px" />
        </div>
      )}

      {isError && (
        <p className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          Não foi possível carregar os casais. Tente novamente mais tarde.
        </p>
      )}

      {data && data.content.length === 0 && (
        <p className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          Nenhum casal vinculado no seu escopo de acesso.
        </p>
      )}

      {data && data.content.length > 0 && (
        <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
          <Table columns={columns} rows={data.content} getRowKey={(casal) => casal.id} />
        </div>
      )}

      <Modal isOpen={modal.isOpen} onClose={modal.close} title="Vincular casal">
        <div className="flex flex-col gap-4">
          <Select
            label="Pessoa 1"
            value={form.pessoa1Id}
            placeholder="Selecione"
            options={(pessoasDisponiveis?.content ?? []).map((pessoa) => ({
              value: pessoa.id,
              label: pessoa.nomeCompleto,
            }))}
            onChange={(event) => form.setPessoa1Id(event.target.value)}
          />
          <Select
            label="Pessoa 2"
            value={form.pessoa2Id}
            placeholder="Selecione"
            options={(pessoasDisponiveis?.content ?? []).map((pessoa) => ({
              value: pessoa.id,
              label: pessoa.nomeCompleto,
            }))}
            onChange={(event) => form.setPessoa2Id(event.target.value)}
          />
          <Input
            label="Data do vínculo"
            type="date"
            value={form.dataVinculo}
            onChange={(event) => form.setDataVinculo(event.target.value)}
          />
          {form.erro && <p className="text-sm text-rose-600">{form.erro}</p>}
          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={modal.close} disabled={form.isSaving}>
              Cancelar
            </Button>
            <Button onClick={form.salvar} disabled={form.isSaving}>
              {form.isSaving ? 'Vinculando…' : 'Vincular'}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  )
}
