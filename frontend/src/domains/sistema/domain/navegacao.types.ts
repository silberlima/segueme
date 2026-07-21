import type { Perfil } from './perfil.types'

export type StatusTela = 'pronto' | 'previsto'

export interface ItemNavegacao {
  titulo: string
  modulo: string
  status: StatusTela
  /** Rota da tela. `null` enquanto a tela estiver "Previsto no escopo" (ainda não implementada). */
  path: string | null
  /** Perfis que veem este item no menu lateral. 'todos' = visível a qualquer perfil autenticado. */
  perfis: Perfil[] | 'todos'
}
