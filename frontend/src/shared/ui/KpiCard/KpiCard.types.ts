import type { ReactNode } from 'react'

/** Tons do valor do KPI: `positivo`/`atencao`/`critico` sinalizam saúde, `neutro` é o padrão. */
export type KpiTone = 'neutro' | 'positivo' | 'atencao' | 'critico'

export interface KpiCardProps {
  rotulo: string
  valor: ReactNode
  nota?: string
  tone?: KpiTone
}
