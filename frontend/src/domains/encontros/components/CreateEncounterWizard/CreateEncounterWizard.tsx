import { Button, Stepper } from '../../../../shared/ui'
import { useCreateEncounterWizard } from './useCreateEncounterWizard'
import { StepIdentidade } from './StepIdentidade'
import { StepLocalDatas } from './StepLocalDatas'
import { StepEquipeDirigente } from './StepEquipeDirigente'
import { StepEquipeComando } from './StepEquipeComando'
import { StepRevisao } from './StepRevisao'

export function CreateEncounterWizard() {
  const wizard = useCreateEncounterWizard()

  if (!wizard.paroquiaId) {
    return (
      <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
        Não foi possível identificar a paróquia do seu perfil. Entre em contato com o suporte para criar um encontro.
      </div>
    )
  }

  const activeIndex = wizard.steps.findIndex((step) => step.id === wizard.activeStepId)
  const isFirstStep = activeIndex === 0
  const isLastStep = activeIndex === wizard.steps.length - 1

  return (
    <div className="flex flex-col gap-6">
      <Stepper
        steps={wizard.steps}
        activeStepId={wizard.activeStepId}
        furthestStepId={wizard.furthestStepId}
        onChange={(stepId) => wizard.goToStep(stepId as typeof wizard.activeStepId)}
      />
      <p className="text-xs font-medium text-slate-400">
        Passo {activeIndex + 1} de {wizard.steps.length}
      </p>

      <div className="rounded-xl border border-slate-200 bg-white p-6">
        {wizard.activeStepId === 'identidade' && (
          <StepIdentidade
            form={wizard.form}
            errors={wizard.errors}
            setField={wizard.setField}
            paroquiaId={wizard.paroquiaId}
          />
        )}
        {wizard.activeStepId === 'local-datas' && (
          <StepLocalDatas form={wizard.form} errors={wizard.errors} setField={wizard.setField} />
        )}
        {wizard.activeStepId === 'equipe-dirigente' && (
          <StepEquipeDirigente
            equipeDirigente={wizard.form.equipeDirigente}
            onAdd={wizard.addMembroDirigente}
            onRemove={wizard.removeMembroDirigente}
          />
        )}
        {wizard.activeStepId === 'equipe-comando' && (
          <StepEquipeComando form={wizard.form} errors={wizard.errors} setField={wizard.setField} />
        )}
        {wizard.activeStepId === 'revisao' && <StepRevisao form={wizard.form} onEditStep={wizard.goToStep} />}
      </div>

      {wizard.submitError && (
        <p className="text-sm text-rose-600">Não foi possível criar o encontro. Tente novamente.</p>
      )}

      <div className="flex items-center justify-between">
        <Button variant="secondary" onClick={wizard.cancelar}>
          Cancelar
        </Button>
        <div className="flex gap-2">
          {!isFirstStep && (
            <Button variant="secondary" onClick={wizard.goBack}>
              Voltar
            </Button>
          )}
          {!isLastStep && <Button onClick={wizard.goNext}>Continuar</Button>}
          {isLastStep && (
            <Button onClick={wizard.submit} disabled={wizard.isSubmitting}>
              {wizard.isSubmitting ? 'Criando...' : 'Criar encontro'}
            </Button>
          )}
        </div>
      </div>
    </div>
  )
}
