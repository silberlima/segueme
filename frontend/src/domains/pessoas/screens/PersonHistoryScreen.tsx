import { Link, useParams } from 'react-router-dom'
import { Skeleton } from '../../../shared/ui'
import { TIPO_EVENTO_LABEL } from '../domain/pessoa.types'
import { useHistoricoPessoa } from '../hooks/useHistoricoPessoa'
import { usePessoa } from '../hooks/usePessoa'

export function PersonHistoryScreen() {
  const { id } = useParams<{ id: string }>()
  const { data: pessoa } = usePessoa(id)
  const { data: eventos, isLoading, isError } = useHistoricoPessoa(id)

  return (
    <div className="p-6">
      <Link to={`/pessoas/${id}`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← {pessoa?.nomeCompleto ?? 'Perfil da pessoa'}
      </Link>

      <h1 className="mb-1 text-xl font-semibold text-slate-900">Histórico</h1>
      <p className="mb-6 text-sm text-slate-500">
        Linha do tempo de eventos da pessoa no movimento (RN-PESSOA-02: nunca apagado).
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
          Não foi possível carregar o histórico desta pessoa.
        </p>
      )}

      {eventos && eventos.length === 0 && (
        <p className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          Nenhum evento registrado ainda.
        </p>
      )}

      {eventos && eventos.length > 0 && (
        <ol className="relative space-y-6 border-l border-slate-200 pl-6">
          {eventos.map((evento) => (
            <li key={evento.id} className="relative">
              <span className="absolute -left-[29px] top-1 h-2.5 w-2.5 rounded-full bg-violet-500" />
              <p className="text-sm font-medium text-slate-900">{TIPO_EVENTO_LABEL[evento.tipo] ?? evento.tipo}</p>
              <p className="text-xs text-slate-500">{new Date(evento.dataEvento).toLocaleString('pt-BR')}</p>
              {evento.descricao && <p className="mt-1 text-sm text-slate-600">{evento.descricao}</p>}
            </li>
          ))}
        </ol>
      )}
    </div>
  )
}
