import type { StepperProps } from './Stepper.types'

export function Stepper({ steps, activeStepId, furthestStepId, onChange }: StepperProps) {
  const activeIndex = steps.findIndex((step) => step.id === activeStepId)
  const furthestIndex = steps.findIndex((step) => step.id === furthestStepId)

  return (
    <ol className="flex items-start">
      {steps.map((step, index) => {
        const isActive = index === activeIndex
        const isCompleted = index < activeIndex
        const isClickable = index <= furthestIndex

        return (
          <li key={step.id} className="flex flex-1 flex-col items-center last:flex-none">
            <div className="flex w-full items-center">
              <span
                className={`h-0.5 flex-1 ${index === 0 ? 'invisible' : ''} ${
                  index <= activeIndex ? 'bg-violet-600' : 'bg-slate-200'
                }`}
              />
              <button
                type="button"
                disabled={!isClickable}
                onClick={() => onChange(step.id)}
                className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-sm font-semibold transition-colors disabled:cursor-not-allowed ${
                  isActive
                    ? 'bg-violet-600 text-white'
                    : isCompleted
                      ? 'bg-violet-100 text-violet-700'
                      : 'bg-slate-100 text-slate-400'
                }`}
              >
                {index + 1}
              </button>
              <span
                className={`h-0.5 flex-1 ${index === steps.length - 1 ? 'invisible' : ''} ${
                  index < activeIndex ? 'bg-violet-600' : 'bg-slate-200'
                }`}
              />
            </div>
            <span className={`mt-2 text-xs font-medium ${isActive ? 'text-violet-700' : 'text-slate-500'}`}>
              {step.label}
            </span>
          </li>
        )
      })}
    </ol>
  )
}
