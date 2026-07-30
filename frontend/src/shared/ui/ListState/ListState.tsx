import { Button } from '../Button'
import { Skeleton } from '../Skeleton'
import type { ListStateProps } from './ListState.types'

/**
 * Os quatro estados de uma lista — carregando (skeleton), com dados, vazio (com orientação de
 * próxima ação) e erro (com nova tentativa) — num componente único (RNF-02, Fase 14). Nenhuma tela
 * deve reimplementar esses blocos: o padrão da Lista de Pessoas vale para todas as listas do
 * sistema, e mudar a aparência de um estado é mudar este arquivo.
 */
export function ListState({
  isLoading = false,
  isError = false,
  isEmpty = false,
  onRetry,
  errorMessage = 'Não foi possível carregar os dados. Tente novamente.',
  emptyMessage = 'Nenhum registro encontrado.',
  emptyAction,
  skeletonRows = 3,
  skeletonHeight = '52px',
  children,
}: ListStateProps) {
  if (isLoading) {
    return (
      <div className="flex flex-col gap-2" role="status" aria-busy="true" aria-label="Carregando">
        {Array.from({ length: skeletonRows }).map((_, index) => (
          <Skeleton key={index} variant="rect" height={skeletonHeight} />
        ))}
      </div>
    )
  }

  if (isError) {
    return (
      <div role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
        <p className={onRetry ? 'mb-3' : undefined}>{errorMessage}</p>
        {onRetry && (
          <Button variant="secondary" onClick={onRetry}>
            Tentar novamente
          </Button>
        )}
      </div>
    )
  }

  if (isEmpty) {
    return (
      <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
        <p className={emptyAction ? 'mb-3' : undefined}>{emptyMessage}</p>
        {emptyAction && <div className="flex flex-wrap gap-2">{emptyAction}</div>}
      </div>
    )
  }

  return <>{children}</>
}
