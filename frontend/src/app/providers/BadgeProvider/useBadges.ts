import { useContext } from 'react'
import { BadgeContext } from './BadgeContext'
import type { BadgeContextValue } from './BadgeProvider.types'

export function useBadges(): BadgeContextValue {
  return useContext(BadgeContext)
}
