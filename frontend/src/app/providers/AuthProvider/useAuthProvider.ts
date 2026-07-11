import { useEffect, useMemo, useState } from 'react'
import { UserManager } from 'oidc-client-ts'
import type { User } from 'oidc-client-ts'
import { setAccessTokenProvider } from '../../../shared/utils/httpClient'
import type { AuthContextValue, AuthUser } from './AuthProvider.types'

function decodeRoles(token: string | undefined): string[] {
  if (!token) return []
  const payloadSegment = token.split('.')[1]
  if (!payloadSegment) return []
  try {
    const payload = JSON.parse(atob(payloadSegment.replace(/-/g, '+').replace(/_/g, '/')))
    const realmRoles = payload.realm_access?.roles
    return Array.isArray(realmRoles) ? realmRoles : []
  } catch {
    return []
  }
}

function toAuthUser(oidcUser: User): AuthUser {
  return {
    id: oidcUser.profile.sub,
    email: oidcUser.profile.email,
    name: oidcUser.profile.name ?? oidcUser.profile.preferred_username,
    accessToken: oidcUser.access_token,
  }
}

function buildUserManager(): UserManager | null {
  const authorityUrl = import.meta.env.VITE_KEYCLOAK_URL
  const realm = import.meta.env.VITE_KEYCLOAK_REALM
  const clientId = import.meta.env.VITE_KEYCLOAK_CLIENT_ID

  if (!authorityUrl || !realm || !clientId) {
    return null
  }

  return new UserManager({
    authority: `${authorityUrl.replace(/\/$/, '')}/realms/${realm}`,
    client_id: clientId,
    redirect_uri: window.location.origin,
    response_type: 'code',
    scope: 'openid profile email',
  })
}

export function useAuthProvider(): AuthContextValue {
  const userManager = useMemo(() => buildUserManager(), [])
  const [user, setUser] = useState<AuthUser | null>(null)
  const [isLoading, setIsLoading] = useState(Boolean(userManager))

  useEffect(() => {
    if (!userManager) {
      setAccessTokenProvider(() => null)
      return
    }

    const handleLoaded = (oidcUser: User) => setUser(toAuthUser(oidcUser))
    const handleUnloaded = () => setUser(null)

    userManager.events.addUserLoaded(handleLoaded)
    userManager.events.addUserUnloaded(handleUnloaded)

    userManager
      .getUser()
      .then((storedUser) => {
        if (storedUser && !storedUser.expired) {
          setUser(toAuthUser(storedUser))
        }
      })
      .finally(() => setIsLoading(false))

    return () => {
      userManager.events.removeUserLoaded(handleLoaded)
      userManager.events.removeUserUnloaded(handleUnloaded)
    }
  }, [userManager])

  useEffect(() => {
    setAccessTokenProvider(() => user?.accessToken ?? null)
  }, [user])

  const roles = useMemo(() => decodeRoles(user?.accessToken), [user])

  return {
    isAuthenticated: Boolean(user),
    isLoading,
    isConfigured: Boolean(userManager),
    user,
    roles,
    login: () => {
      if (!userManager) {
        console.warn('AuthProvider: Keycloak não configurado (VITE_KEYCLOAK_*). Login ignorado.')
        return
      }
      void userManager.signinRedirect()
    },
    logout: () => {
      if (!userManager) {
        setUser(null)
        return
      }
      void userManager.signoutRedirect()
    },
  }
}
