import type { Perfil } from './perfil.types'

export interface MeResponse {
  id: string
  email: string
  nome: string
  perfis: Perfil[]
}
