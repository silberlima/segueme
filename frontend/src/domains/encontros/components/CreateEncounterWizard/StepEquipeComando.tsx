import { CasalSearchSelect } from '../CasalSearchSelect'
import { PersonSearchSelect } from '../PersonSearchSelect'
import type { WizardState } from './wizard.types'

interface StepEquipeComandoProps {
  form: WizardState
  errors: Record<string, string>
  setField: <K extends keyof WizardState>(field: K, value: WizardState[K]) => void
}

export function StepEquipeComando({ form, errors, setField }: StepEquipeComandoProps) {
  return (
    <div className="flex flex-col gap-4">
      <PersonSearchSelect
        label="Coordenador geral"
        selectedId={form.coordenadorGeralId}
        selectedLabel={form.coordenadorGeralNome}
        error={errors.coordenadorGeralId}
        onSelect={(id, nome) => {
          setField('coordenadorGeralId', id)
          setField('coordenadorGeralNome', nome)
        }}
        onClear={() => {
          setField('coordenadorGeralId', '')
          setField('coordenadorGeralNome', '')
        }}
      />
      <CasalSearchSelect
        label="Casal coordenador"
        selectedId={form.casalCoordenadorId}
        selectedLabel={form.casalCoordenadorNome}
        error={errors.casalCoordenadorId}
        onSelect={(id, label) => {
          setField('casalCoordenadorId', id)
          setField('casalCoordenadorNome', label)
        }}
        onClear={() => {
          setField('casalCoordenadorId', '')
          setField('casalCoordenadorNome', '')
        }}
      />
      <p className="text-xs text-slate-500">
        Após a criação, a Comando enviará convites aos coordenadores de cada equipe; toda recusa exige justificativa
        e notifica imediatamente a Montagem.
      </p>
    </div>
  )
}
