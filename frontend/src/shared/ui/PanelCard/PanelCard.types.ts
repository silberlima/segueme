import type { ReactNode } from 'react'

export interface PanelCardProps {
  titulo: string
  /** Atalho "Ver todos" do recorte top N (`dashboards-por-perfil.md` 2). */
  acao?: ReactNode
  children: ReactNode
}
