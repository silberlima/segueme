export interface CasalSearchSelectProps {
  label?: string
  selectedId: string
  selectedLabel: string
  onSelect: (casalId: string, label: string) => void
  onClear?: () => void
  error?: string
  placeholder?: string
}
