import { iniciaisDoNome } from '../../domain/pessoa.entity'
import type { PersonAvatarProps } from './PersonAvatar.types'

const SIZE_CLASSES: Record<NonNullable<PersonAvatarProps['size']>, string> = {
  sm: 'h-8 w-8 text-xs',
  md: 'h-12 w-12 text-sm',
}

export function PersonAvatar({ nomeCompleto, size = 'sm' }: PersonAvatarProps) {
  return (
    <span
      className={`inline-flex shrink-0 items-center justify-center rounded-full bg-violet-100 font-semibold text-violet-700 ${SIZE_CLASSES[size]}`}
    >
      {iniciaisDoNome(nomeCompleto)}
    </span>
  )
}
