import type { ChangeEvent } from 'react'

export interface InputProps {
  label?: string
  name?: string
  type?: 'text' | 'email' | 'password' | 'number' | 'tel' | 'date'
  value?: string
  placeholder?: string
  /** Limite de caracteres para campos de tamanho fixo, como a UF do endereço. */
  maxLength?: number
  error?: string
  disabled?: boolean
  onChange?: (event: ChangeEvent<HTMLInputElement>) => void
}
