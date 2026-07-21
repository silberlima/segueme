import { useContext } from 'react'
import { ProfileContext } from './ProfileContext'
import type { ProfileContextValue } from './ProfileProvider.types'

export function useProfile(): ProfileContextValue {
  const context = useContext(ProfileContext)
  if (!context) {
    throw new Error('useProfile deve ser usado dentro de um ProfileProvider')
  }
  return context
}
