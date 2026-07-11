import type { BadgeProps, BadgeTone } from './Badge.types'

const toneClasses: Record<BadgeTone, string> = {
  neutral: 'bg-slate-100 text-slate-700',
  success: 'bg-emerald-100 text-emerald-700',
  warning: 'bg-amber-100 text-amber-700',
  danger: 'bg-rose-100 text-rose-700',
  info: 'bg-sky-100 text-sky-700',
}

export function Badge({ tone = 'neutral', children }: BadgeProps) {
  return (
    <span
      className={`inline-flex items-center h-6 px-2.5 rounded-full text-xs font-medium ${toneClasses[tone]}`}
    >
      {children}
    </span>
  )
}
