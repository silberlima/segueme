import { ProfileContext } from './ProfileContext'
import { useProfileProvider } from './useProfileProvider'
import type { ProfileProviderProps } from './ProfileProvider.types'

export function ProfileProvider({ children }: ProfileProviderProps) {
  const value = useProfileProvider()

  return <ProfileContext.Provider value={value}>{children}</ProfileContext.Provider>
}
