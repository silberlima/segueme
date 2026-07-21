import type { ReactNode } from 'react'
import type { Perfil } from '../../../domains/sistema/domain/perfil.types'

export interface ProfileProviderProps {
  children: ReactNode
}

export interface ProfileContextValue {
  isLoading: boolean
  perfis: Perfil[]
  perfilAtivo: Perfil | null
  setPerfilAtivo: (perfil: Perfil) => void
}
