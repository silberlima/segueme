import type { ReactNode } from 'react'

export interface PageHeaderProps {
  titulo: string
  /** Escopo do painel: paróquia, setor, diocese ou Encontro corrente. */
  subtitulo?: string
  acoes?: ReactNode
}
