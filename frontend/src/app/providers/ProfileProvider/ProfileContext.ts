import { createContext } from 'react'
import type { ProfileContextValue } from './ProfileProvider.types'

export const ProfileContext = createContext<ProfileContextValue | undefined>(undefined)
