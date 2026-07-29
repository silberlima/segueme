import { useState } from 'react'
import { Button, Skeleton, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { FrequenciaBar } from '../components/FrequenciaBar'
import { ParoquiaSelect } from '../components/ParoquiaSelect'
import type { FrequenciaPessoa } from '../domain/posencontro.types'
import { useFrequencia } from '../hooks/useFrequencia'

/**
 * Frequência (`screen_frequencia`, RF-059): histórico de frequência por pessoa e período. Tela
 * desenhada nesta fase (pendência #20 de `pendencias-gerais.md`).
 *
 * <p>O denominador é o total de encontros pós que a paróquia realizou no período — quem faltou sem
 * ter presença registrada conta como ausente, como nos exemplos de RN-POS-03 (3/12). Sem filtro de
 * período, a apuração vai do início dos registros até hoje.
 */
export function FrequencyScreen() {
  const [paroquiaId, setParoquiaId] = useState('')
  const [de, setDe] = useState('')
  const [ate, setAte] = useState('')

  const {
    data: frequencias,
    isLoading,
    isError,
    refetch,
  } = useFrequencia({ paroquiaId, de: de || undefined, ate: ate || undefined })

  const columns: TableColumn<FrequenciaPessoa>[] = [
    { key: 'pessoa', header: 'Jovem', render: (frequencia) => frequencia.pessoaNome },
    {
      key: 'presencas',
      header: 'Presenças',
      render: (frequencia) => `${frequencia.presencas} de ${frequencia.totalEncontros}`,
    },
    {
      key: 'frequencia',
      header: 'Frequência',
      render: (frequencia) => (
        <FrequenciaBar percentual={frequencia.percentual} faixa={frequencia.faixaPrioridade} />
      ),
    },
  ]

  return (
    <div className="p-6">
      <h1 className="mb-6 text-xl font-semibold text-slate-900">Frequência</h1>

      <div className="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-3">
        <ParoquiaSelect value={paroquiaId} onChange={setParoquiaId} />

        <label className="flex flex-col gap-1.5">
          <span className="text-sm font-medium text-slate-700">De</span>
          <input
            type="date"
            value={de}
            onChange={(event) => setDe(event.target.value)}
            className="h-10 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none focus:border-violet-500"
          />
        </label>

        <label className="flex flex-col gap-1.5">
          <span className="text-sm font-medium text-slate-700">Até</span>
          <input
            type="date"
            value={ate}
            onChange={(event) => setAte(event.target.value)}
            className="h-10 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none focus:border-violet-500"
          />
        </label>
      </div>

      {isLoading && <Skeleton variant="rect" height="120px" />}

      {isError && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          <p className="mb-3">Não foi possível carregar a frequência.</p>
          <Button variant="secondary" onClick={() => refetch()}>
            Tentar novamente
          </Button>
        </div>
      )}

      {!paroquiaId && (
        <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          Selecione a paróquia para ver a frequência dos jovens acompanhados.
        </div>
      )}

      {paroquiaId && frequencias && frequencias.length === 0 && (
        <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          Nenhum jovem acompanhado nesta paróquia ainda.
        </div>
      )}

      {frequencias && frequencias.length > 0 && (
        <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
          <Table columns={columns} rows={frequencias} getRowKey={(frequencia) => frequencia.pessoaId} />
        </div>
      )}
    </div>
  )
}
