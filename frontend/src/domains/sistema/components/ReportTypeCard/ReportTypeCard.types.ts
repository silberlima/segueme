import type { TipoDeRelatorio, TipoRelatorio } from '../../domain/relatorio.types'

export interface ReportTypeCardProps {
  tipoDeRelatorio: TipoDeRelatorio
  gerando: boolean
  onGerar: (tipo: TipoRelatorio) => void
}
