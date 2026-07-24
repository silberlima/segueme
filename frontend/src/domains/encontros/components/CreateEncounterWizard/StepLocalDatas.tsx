import { Input } from '../../../../shared/ui'
import type { WizardState } from './wizard.types'

interface StepLocalDatasProps {
  form: WizardState
  errors: Record<string, string>
  setField: <K extends keyof WizardState>(field: K, value: WizardState[K]) => void
}

export function StepLocalDatas({ form, errors, setField }: StepLocalDatasProps) {
  return (
    <div className="flex flex-col gap-4">
      <Input
        label="Local"
        value={form.local}
        error={errors.local}
        onChange={(event) => setField('local', event.target.value)}
      />
      <div className="flex gap-4">
        <Input
          label="Data de início"
          type="date"
          value={form.dataInicio}
          error={errors.dataInicio}
          onChange={(event) => setField('dataInicio', event.target.value)}
        />
        <Input
          label="Data de término"
          type="date"
          value={form.dataTermino}
          error={errors.dataTermino}
          onChange={(event) => setField('dataTermino', event.target.value)}
        />
      </div>
      <div className="w-40">
        <Input
          label="Vagas"
          type="number"
          value={form.vagas}
          error={errors.vagas}
          onChange={(event) => setField('vagas', event.target.value)}
        />
      </div>
      <p className="text-xs text-slate-500">
        Ao atingir o limite de vagas, novas pré-inscrições entram automaticamente no Cadastro de Reserva, com
        prioridade no próximo encontro (RN-16).
      </p>
    </div>
  )
}
