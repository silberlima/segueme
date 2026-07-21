import { Badge, Skeleton, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { useDioceses } from '../hooks/useDioceses'
import type { Diocese } from '../domain/hierarquia.types'

type Nivel = 'Nacional' | 'Diocese' | 'Setor' | 'Paróquia'

interface LinhaHierarquia {
  key: string
  nivel: Nivel
  nome: string
  numParoquias: number | null
  numSeguidores: number | null
  depth: number
}

const NIVEL_TONE: Record<Nivel, 'info' | 'neutral'> = {
  Nacional: 'info',
  Diocese: 'info',
  Setor: 'neutral',
  Paróquia: 'neutral',
}

function achatar(dioceses: Diocese[]): LinhaHierarquia[] {
  const linhas: LinhaHierarquia[] = [
    {
      key: 'nacional',
      nivel: 'Nacional',
      nome: 'Nacional',
      numParoquias: dioceses.reduce((soma, diocese) => soma + diocese.numParoquias, 0),
      numSeguidores: dioceses.reduce((soma, diocese) => soma + diocese.numSeguidores, 0),
      depth: 0,
    },
  ]

  for (const diocese of dioceses) {
    linhas.push({
      key: diocese.id,
      nivel: 'Diocese',
      nome: diocese.nome,
      numParoquias: diocese.numParoquias,
      numSeguidores: diocese.numSeguidores,
      depth: 1,
    })
    for (const setor of diocese.setores) {
      linhas.push({
        key: setor.id,
        nivel: 'Setor',
        nome: setor.nome,
        numParoquias: setor.numParoquias,
        numSeguidores: setor.numSeguidores,
        depth: 2,
      })
      for (const paroquia of setor.paroquias) {
        linhas.push({
          key: paroquia.id,
          nivel: 'Paróquia',
          nome: paroquia.nome,
          numParoquias: null,
          numSeguidores: null,
          depth: 3,
        })
      }
    }
  }

  return linhas
}

const columns: TableColumn<LinhaHierarquia>[] = [
  {
    key: 'nivel',
    header: 'Nível',
    render: (linha) => <Badge tone={NIVEL_TONE[linha.nivel]}>{linha.nivel}</Badge>,
  },
  {
    key: 'nome',
    header: 'Nome',
    render: (linha) => <span style={{ paddingLeft: `${linha.depth * 16}px` }}>{linha.nome}</span>,
  },
  {
    key: 'numParoquias',
    header: 'Paróquias',
    render: (linha) => (linha.numParoquias === null ? '—' : linha.numParoquias),
  },
  {
    key: 'numSeguidores',
    header: 'Seguidores',
    render: (linha) => (linha.numSeguidores === null ? '—' : linha.numSeguidores),
  },
]

export function DiocesesScreen() {
  const { data, isLoading, isError } = useDioceses(0, 100)

  return (
    <div className="p-6">
      <h1 className="mb-1 text-xl font-semibold text-slate-900">Dioceses e Setores</h1>
      <p className="mb-6 text-sm text-slate-500">
        Estrutura hierárquica Nacional → Diocese → Setor → Paróquia.
      </p>

      {isLoading && (
        <div className="flex flex-col gap-2">
          <Skeleton variant="text" />
          <Skeleton variant="text" />
          <Skeleton variant="text" />
        </div>
      )}

      {isError && (
        <p className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          Não foi possível carregar a estrutura hierárquica. Tente novamente mais tarde.
        </p>
      )}

      {data && data.content.length === 0 && (
        <p className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          Nenhuma diocese encontrada para o seu escopo de acesso.
        </p>
      )}

      {data && data.content.length > 0 && (
        <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
          <Table columns={columns} rows={achatar(data.content)} getRowKey={(linha) => linha.key} />
        </div>
      )}
    </div>
  )
}
