import { useContext } from 'react'
import { AuthContext } from './AuthContext'
import type { AuthContextValue } from './AuthProvider.types'

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de um AuthProvider')
  }
  return context
}
