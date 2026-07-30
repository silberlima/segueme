import { Badge } from '../../../../shared/ui'
import { percentualOcupacao } from '../../domain/dashboard.entity'
import type { TeamStatusListProps } from './TeamStatusList.types'

/**
 * Lista "Equipes do encontro" com barra de status e contagem atual/máxima (RN-06). É o mesmo
 * checklist de equipes completas que o Painel da Paróquia exibe.
 */
export function TeamStatusList({ equipes }: TeamStatusListProps) {
  if (equipes.length === 0) {
    return <p className="text-sm text-slate-500">Nenhuma equipe montada neste Encontro.</p>
  }

  return (
    <ul className="flex flex-col gap-3">
      {equipes.map((equipe) => (
        <li key={equipe.id}>
          <div className="mb-1 flex items-center justify-between gap-2 text-sm">
            <span className="font-medium text-slate-900">{equipe.nome}</span>
            <span className="flex items-center gap-2 text-slate-600">
              {equipe.preenchidos}/{equipe.maximo}
              <Badge tone={equipe.completa ? 'success' : 'warning'}>
                {equipe.completa ? 'Completa' : 'Incompleta'}
              </Badge>
            </span>
          </div>
          <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100">
            <div
              className={`h-full rounded-full transition-all ${equipe.completa ? 'bg-emerald-500' : 'bg-amber-500'}`}
              style={{ width: `${percentualOcupacao(equipe)}%` }}
            />
          </div>
        </li>
      ))}
    </ul>
  )
}
