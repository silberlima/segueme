import { Link, useParams } from 'react-router-dom'
import { ListState } from '../../../shared/ui'
import { TIPO_EVENTO_LABEL } from '../domain/pessoa.types'
import { useHistoricoPessoa } from '../hooks/useHistoricoPessoa'
import { usePessoa } from '../hooks/usePessoa'

export function PersonHistoryScreen() {
  const { id } = useParams<{ id: string }>()
  const { data: pessoa } = usePessoa(id)
  const { data: eventos, isLoading, isError, refetch } = useHistoricoPessoa(id)

  return (
    <div className="p-6">
      <Link to={`/pessoas/${id}`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← {pessoa?.nomeCompleto ?? 'Perfil da pessoa'}
      </Link>

      <h1 className="mb-1 text-xl font-semibold text-slate-900">Histórico</h1>
      <p className="mb-6 text-sm text-slate-500">
        Linha do tempo de eventos da pessoa no movimento (RN-PESSOA-02: nunca apagado).
      </p>

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={eventos?.length === 0}
        onRetry={() => refetch()}
        errorMessage="Não foi possível carregar o histórico desta pessoa."
        emptyMessage="Nenhum evento registrado ainda."
      >
        <ol className="relative space-y-6 border-l border-slate-200 pl-6">
          {(eventos ?? []).map((evento) => (
            <li key={evento.id} className="relative">
              <span className="absolute -left-[29px] top-1 h-2.5 w-2.5 rounded-full bg-violet-500" />
              <p className="text-sm font-medium text-slate-900">{TIPO_EVENTO_LABEL[evento.tipo] ?? evento.tipo}</p>
              <p className="text-xs text-slate-500">{new Date(evento.dataEvento).toLocaleString('pt-BR')}</p>
              {evento.descricao && <p className="mt-1 text-sm text-slate-600">{evento.descricao}</p>}
            </li>
          ))}
        </ol>
      </ListState>
    </div>
  )
}
