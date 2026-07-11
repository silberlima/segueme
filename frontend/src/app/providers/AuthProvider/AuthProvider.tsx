import { AuthContext } from './AuthContext'
import { useAuthProvider } from './useAuthProvider'
import type { AuthProviderProps } from './AuthProvider.types'

export function AuthProvider({ children }: AuthProviderProps) {
  const value = useAuthProvider()

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
