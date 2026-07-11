import type { ChangeEvent } from 'react'

export interface SelectOption {
  value: string
  label: string
}

export interface SelectProps {
  label?: string
  name?: string
  value?: string
  options: SelectOption[]
  placeholder?: string
  error?: string
  disabled?: boolean
  onChange?: (event: ChangeEvent<HTMLSelectElement>) => void
}
