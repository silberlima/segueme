export interface PersonSearchSelectProps {
  label?: string
  selectedId: string
  selectedLabel: string
  onSelect: (pessoaId: string, nomeCompleto: string) => void
  onClear?: () => void
  error?: string
  placeholder?: string
  /** Restringe a busca a pessoas desta paróquia (ex.: diretor espiritual). */
  paroquiaId?: string
}
