import { Button } from '../../../../shared/ui'
import type { ReportTypeCardProps } from './ReportTypeCard.types'

/** Cartão de um tipo de relatório (RF-056): descrição do que sai e o botão que dispara a geração. */
export function ReportTypeCard({ tipoDeRelatorio, gerando, onGerar }: ReportTypeCardProps) {
  return (
    <section
      aria-label={tipoDeRelatorio.titulo}
      className="flex flex-col justify-between gap-4 rounded-xl border border-slate-200 bg-white p-4"
    >
      <div>
        <h2 className="text-sm font-semibold text-slate-900">{tipoDeRelatorio.titulo}</h2>
        <p className="mt-1 text-xs text-slate-500">{tipoDeRelatorio.descricao}</p>
      </div>
      <div>
        <Button disabled={gerando} onClick={() => onGerar(tipoDeRelatorio.tipo)}>
          {gerando ? 'Gerando...' : 'Gerar relatório'}
        </Button>
      </div>
    </section>
  )
}
