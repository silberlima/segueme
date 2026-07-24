export interface StepperStep {
  id: string
  label: string
}

export interface StepperProps {
  steps: readonly StepperStep[]
  activeStepId: string
  /** Índice do passo mais avançado já validado — passos além dele não são clicáveis. */
  furthestStepId: string
  onChange: (stepId: string) => void
}
