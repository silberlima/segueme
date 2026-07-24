import { Input } from '../../../../shared/ui'
import { PersonSearchSelect } from '../PersonSearchSelect'
import type { WizardState } from './wizard.types'

interface StepIdentidadeProps {
  form: WizardState
  errors: Record<string, string>
  setField: <K extends keyof WizardState>(field: K, value: WizardState[K]) => void
  paroquiaId: string
}

export function StepIdentidade({ form, errors, setField, paroquiaId }: StepIdentidadeProps) {
  return (
    <div className="flex flex-col gap-4">
      <div className="w-40">
        <Input label="Número do encontro" value="Gerado automaticamente" disabled />
      </div>
      <Input
        label="Padroeiro do encontro"
        value={form.padroeiro}
        error={errors.padroeiro}
        onChange={(event) => setField('padroeiro', event.target.value)}
      />
      <PersonSearchSelect
        label="Diretor espiritual"
        selectedId={form.diretorEspiritualId}
        selectedLabel={form.diretorEspiritualNome}
        paroquiaId={paroquiaId}
        error={errors.diretorEspiritualId}
        onSelect={(id, nome) => {
          setField('diretorEspiritualId', id)
          setField('diretorEspiritualNome', nome)
        }}
        onClear={() => {
          setField('diretorEspiritualId', '')
          setField('diretorEspiritualNome', '')
        }}
      />
    </div>
  )
}
