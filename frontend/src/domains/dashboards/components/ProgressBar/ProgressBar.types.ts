export type ProgressTone = 'violeta' | 'verde' | 'ambar'

export interface ProgressBarProps {
  rotulo: string
  detalhe: string
  percentual: number
  tone?: ProgressTone
}
