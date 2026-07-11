import type { ChangeEvent } from 'react'

export interface InputProps {
  label?: string
  name?: string
  type?: 'text' | 'email' | 'password' | 'number' | 'tel' | 'date'
  value?: string
  placeholder?: string
  error?: string
  disabled?: boolean
  onChange?: (event: ChangeEvent<HTMLInputElement>) => void
}
