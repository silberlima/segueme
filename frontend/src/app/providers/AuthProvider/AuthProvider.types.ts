import type { ReactNode } from 'react'

export interface AuthProviderProps {
  children: ReactNode
}

export interface AuthUser {
  id?: string
  email?: string
  name?: string
  accessToken?: string
}

export interface AuthContextValue {
  isAuthenticated: boolean
  isLoading: boolean
  isConfigured: boolean
  user: AuthUser | null
  roles: string[]
  login: (email: string, password: string) => Promise<void>
  logout: () => void
}
