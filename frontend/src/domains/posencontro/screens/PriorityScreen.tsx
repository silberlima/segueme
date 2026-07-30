import { useState } from 'react'
import { Badge, ListState, Table } from '../../../shared/ui'
import type { BadgeTone } from '../../../shared/ui/Badge/Badge.types'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { FrequenciaBar } from '../components/FrequenciaBar'
import { ParoquiaSelect } from '../components/ParoquiaSelect'
import { FAIXA_PRIORIDADE_LABEL } from '../domain/posencontro.types'
import type { FaixaPrioridade, FrequenciaPessoa } from '../domain/posencontro.types'
import { usePrioridades } from '../hooks/usePrioridades'

const tonePorFaixa: Record<FaixaPrioridade, BadgeTone> = {
  ALTA: 'success',
  MEDIA: 'warning',
  BAIXA: 'danger',
}

/**
 * Prioridades (`screen_prioridades`, RF-059, RN-POS-02/03): ranking dos jovens acompanhados por
 * frequência acumulada, com a faixa de prioridade para as próximas equipes. Tela desenhada nesta
 * fase (pendência #20 de `pendencias-gerais.md`).
 *
 * <p>A régua que separa Alta/Média/Baixa é parametrizável no backend enquanto o PO não a validar
 * (pendência #14) — a tela só exibe a faixa apurada, nunca recalcula o corte.
 */
export function PriorityScreen() {
  const [paroquiaId, setParoquiaId] = useState('')
  const { data: prioridades, isLoading, isError, refetch } = usePrioridades(paroquiaId)

  const columns: TableColumn<FrequenciaPessoa>[] = [
    {
      key: 'posicao',
      header: '#',
      render: (prioridade) => String((prioridades ?? []).indexOf(prioridade) + 1),
    },
    { key: 'pessoa', header: 'Jovem', render: (prioridade) => prioridade.pessoaNome },
    {
      key: 'presencas',
      header: 'Presenças',
      render: (prioridade) => `${prioridade.presencas} de ${prioridade.totalEncontros}`,
    },
    {
      key: 'frequencia',
      header: 'Frequência',
      render: (prioridade) => (
        <FrequenciaBar percentual={prioridade.percentual} faixa={prioridade.faixaPrioridade} />
      ),
    },
    {
      key: 'faixa',
      header: 'Prioridade',
      render: (prioridade) => (
        <Badge tone={tonePorFaixa[prioridade.faixaPrioridade]}>
          {FAIXA_PRIORIDADE_LABEL[prioridade.faixaPrioridade]}
        </Badge>
      ),
    },
  ]

  return (
    <div className="p-6">
      <h1 className="mb-2 text-xl font-semibold text-slate-900">Prioridades</h1>
      <p className="mb-6 text-sm text-slate-500">
        Quem participa com frequência tem prioridade para trabalhar nas equipes.
      </p>

      <div className="mb-8 max-w-sm">
        <ParoquiaSelect value={paroquiaId} onChange={setParoquiaId} />
      </div>

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={!paroquiaId || prioridades?.length === 0}
        onRetry={() => refetch()}
        skeletonRows={1}
        skeletonHeight="120px"
        errorMessage="Não foi possível carregar o ranking de prioridade."
        emptyMessage={
          paroquiaId
            ? 'Nenhum jovem acompanhado nesta paróquia ainda.'
            : 'Selecione a paróquia para ver o ranking de prioridade.'
        }
      >
        <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
          <Table columns={columns} rows={prioridades ?? []} getRowKey={(prioridade) => prioridade.pessoaId} />
        </div>
      </ListState>
    </div>
  )
}
