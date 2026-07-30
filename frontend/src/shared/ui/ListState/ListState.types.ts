import type { ReactNode } from 'react'

export interface ListStateProps {
  /** Carregando: exibe `skeletonRows` skeletons de altura `skeletonHeight`. */
  isLoading?: boolean
  /** Erro: exibe `errorMessage` e, se houver `onRetry`, o botão de nova tentativa. */
  isError?: boolean
  /** Vazio: exibe `emptyMessage` e `emptyAction`. Só é avaliado fora de carregando/erro. */
  isEmpty?: boolean
  /** Ação de nova tentativa do estado de erro; sem ela o estado de erro não oferece botão. */
  onRetry?: () => void
  errorMessage?: string
  /** Orientação de próxima ação do estado vazio (RNF-02). */
  emptyMessage?: ReactNode
  /** Botões do estado vazio, ao lado da orientação. */
  emptyAction?: ReactNode
  skeletonRows?: number
  skeletonHeight?: string
  /**
   * Conteúdo do estado com dados. Como o JSX é montado pelo chamador antes de chegar aqui, ele
   * precisa ser seguro contra dados ausentes (`data?.itens ?? []`) mesmo que só seja exibido no
   * estado com dados.
   */
  children?: ReactNode
}
