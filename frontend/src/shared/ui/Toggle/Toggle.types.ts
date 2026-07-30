export interface ToggleProps {
  /** Rótulo acessível do interruptor — é por ele que o E2E e o leitor de tela o encontram. */
  label: string
  descricao?: string
  checked: boolean
  disabled?: boolean
  onChange: (checked: boolean) => void
}
