import type { ReactNode } from 'react'

export interface BadgeProviderProps {
  children: ReactNode
}

export interface BadgeContextValue {
  /** Contagem de pendências por rota do menu lateral (RN-DASH-02). */
  badges: Record<string, number>
}
