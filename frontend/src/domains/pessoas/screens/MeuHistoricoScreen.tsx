import { Link } from 'react-router-dom'
import { ListState, PageHeader } from '../../../shared/ui'
import { TIPO_EVENTO_LABEL } from '../domain/pessoa.types'
import { useMeuHistorico } from '../hooks/useMeusDados'

/**
 * Meu Histórico: a linha do tempo da própria pessoa, somente leitura. O histórico é append-only e
 * nasce dos fluxos do movimento (RN-PESSOA-02) — não há o que editar aqui.
 */
export function MeuHistoricoScreen() {
  const { data: eventos, isLoading, isError, refetch } = useMeuHistorico()

  return (
    <div className="p-6">
      <Link to="/meus-dados" className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Meus Dados
      </Link>

      <PageHeader titulo="Meu Histórico" subtitulo="Sua trajetória no movimento, registrada e nunca apagada" />

      <ListState
        isLoading={isLoading}
        isError={isError}
        isEmpty={eventos?.length === 0}
        onRetry={() => refetch()}
        errorMessage="Não foi possível carregar o seu histórico."
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
