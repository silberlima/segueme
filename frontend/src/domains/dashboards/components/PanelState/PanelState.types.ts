export interface PanelStateProps {
  isLoading: boolean
  isError: boolean
  /** Mensagem do estado vazio; quando ausente, nada é exibido no lugar dos dados. */
  vazio?: string
}
