import { Skeleton } from '../../../../shared/ui'
import type { PanelStateProps } from './PanelState.types'

/**
 * Os quatro estados de um painel (carregando/dados/vazio/erro, RNF-02) num único componente, para
 * que os nove painéis não reimplementem cada um o seu.
 */
export function PanelState({ isLoading, isError, vazio }: PanelStateProps) {
  if (isLoading) {
    return <Skeleton variant="rect" height="120px" />
  }

  if (isError) {
    return (
      <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
        Não foi possível carregar o painel. Tente novamente.
      </div>
    )
  }

  if (vazio) {
    return <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">{vazio}</div>
  }

  return null
}
