import type { SkeletonProps, SkeletonVariant } from './Skeleton.types'

const variantClasses: Record<SkeletonVariant, string> = {
  text: 'rounded h-4 w-full',
  circle: 'rounded-full h-10 w-10',
  rect: 'rounded-lg h-24 w-full',
}

export function Skeleton({ variant = 'text', width, height }: SkeletonProps) {
  return (
    <div
      className={`animate-pulse bg-slate-200 ${variantClasses[variant]}`}
      style={{ width, height }}
    />
  )
}
