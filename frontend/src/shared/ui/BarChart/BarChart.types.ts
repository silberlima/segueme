export interface BarChartItem {
  rotulo: string
  valor: number
}

export interface BarChartProps {
  titulo: string
  itens: BarChartItem[]
  /** Formatação do valor exibido à direita da barra; por padrão, o número puro. */
  formatarValor?: (valor: number) => string
  mensagemVazio?: string
}
