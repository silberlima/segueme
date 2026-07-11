import { createContext } from 'react'
import type { AuthContextValue } from './AuthProvider.types'

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
