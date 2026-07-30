import { createContext } from 'react'
import type { BadgeContextValue } from './BadgeProvider.types'

export const BadgeContext = createContext<BadgeContextValue>({ badges: {} })
