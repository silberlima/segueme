export interface PessoaSearchSelectProps {
  label?: string
  selectedId: string
  selectedLabel: string
  onSelect: (pessoaId: string, nomeCompleto: string) => void
  onClear?: () => void
  error?: string
  placeholder?: string
}
