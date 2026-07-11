import type { ReactNode } from 'react'

export interface ProtectedRouteProps {
  roles?: string[]
  children: ReactNode
}
