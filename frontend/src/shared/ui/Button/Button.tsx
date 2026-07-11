import type { ButtonProps, ButtonVariant } from './Button.types'

const variantClasses: Record<ButtonVariant, string> = {
  primary: 'bg-violet-600 text-white hover:bg-violet-700',
  secondary: 'bg-white border border-slate-200 text-slate-900 hover:border-slate-300',
  danger: 'bg-white border border-slate-200 text-rose-600 hover:border-rose-300',
}

export function Button({
  variant = 'primary',
  disabled,
  type = 'button',
  onClick,
  children,
}: ButtonProps) {
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={`h-10 px-4 rounded-lg text-sm font-semibold transition-colors disabled:opacity-50 ${variantClasses[variant]}`}
    >
      {children}
    </button>
  )
}
