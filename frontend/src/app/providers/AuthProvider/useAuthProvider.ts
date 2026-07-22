import { useCallback, useEffect, useRef, useState } from 'react'
import { setAccessTokenProvider } from '../../../shared/utils/httpClient'
import { queryClient } from '../QueryProvider'
import type { AuthContextValue, AuthUser } from './AuthProvider.types'

const SESSION_STORAGE_KEY = 'segueme.session'
const REFRESH_MARGIN_MS = 30_000

interface StoredSession {
  accessToken: string
  refreshToken?: string
  expiresAt: number
}

interface TokenResponse {
  access_token: string
  refresh_token?: string
  expires_in: number
}

interface JwtClaims {
  sub?: string
  email?: string
  name?: string
  preferred_username?: string
  realm_access?: { roles?: string[] }
}

function decodeJwt<T>(token: string): T | null {
  const payload = token.split('.')[1]
  if (!payload) return null
  try {
    return JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/'))) as T
  } catch {
    return null
  }
}

function buildAuthority(): string | null {
  const authorityUrl = import.meta.env.VITE_KEYCLOAK_URL
  const realm = import.meta.env.VITE_KEYCLOAK_REALM
  if (!authorityUrl || !realm) return null
  return `${authorityUrl.replace(/\/$/, '')}/realms/${realm}`
}

function readStoredSession(): StoredSession | null {
  const raw = sessionStorage.getItem(SESSION_STORAGE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as StoredSession
  } catch {
    return null
  }
}

function writeStoredSession(session: StoredSession | null): void {
  if (session) {
    sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session))
  } else {
    sessionStorage.removeItem(SESSION_STORAGE_KEY)
  }
}

function toStoredSession(tokenResponse: TokenResponse): StoredSession {
  return {
    accessToken: tokenResponse.access_token,
    refreshToken: tokenResponse.refresh_token,
    expiresAt: Date.now() + tokenResponse.expires_in * 1000,
  }
}

function toAuthUser(accessToken: string): AuthUser {
  const claims = decodeJwt<JwtClaims>(accessToken) ?? {}
  return {
    id: claims.sub,
    email: claims.email,
    name: claims.name ?? claims.preferred_username,
    accessToken,
  }
}

function toRoles(accessToken: string): string[] {
  return decodeJwt<JwtClaims>(accessToken)?.realm_access?.roles ?? []
}

/**
 * Login nativo via Direct Access Grant do Keycloak (RFC 6749 Resource Owner Password
 * Credentials): a tela React coleta e-mail/senha e troca direto com o token endpoint,
 * sem redirecionar para a UI hospedada do Keycloak. A senha nunca é persistida pelo
 * frontend — só o access/refresh token retornado pelo Keycloak.
 */
export function useAuthProvider(): AuthContextValue {
  const clientId = import.meta.env.VITE_KEYCLOAK_CLIENT_ID as string | undefined
  const isConfigured = Boolean(buildAuthority() && clientId)

  const [session, setSession] = useState<StoredSession | null>(() => {
    const stored = readStoredSession()
    return stored && stored.expiresAt > Date.now() ? stored : null
  })
  const [isLoading, setIsLoading] = useState<boolean>(() => {
    const stored = readStoredSession()
    return Boolean(stored && stored.expiresAt <= Date.now() && stored.refreshToken)
  })
  const refreshTimer = useRef<number | undefined>(undefined)

  const applyTokenResponse = useCallback((tokenResponse: TokenResponse) => {
    const next = toStoredSession(tokenResponse)
    writeStoredSession(next)
    setSession(next)
  }, [])

  const clearSession = useCallback(() => {
    writeStoredSession(null)
    setSession(null)
  }, [])

  const refresh = useCallback(
    async (refreshToken: string) => {
      const authority = buildAuthority()
      if (!authority || !clientId) return
      const response = await fetch(`${authority}/protocol/openid-connect/token`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
          grant_type: 'refresh_token',
          client_id: clientId,
          refresh_token: refreshToken,
        }),
      })
      if (!response.ok) {
        clearSession()
        return
      }
      applyTokenResponse((await response.json()) as TokenResponse)
    },
    [applyTokenResponse, clearSession, clientId],
  )

  useEffect(() => {
    // O estado inicial de session/isLoading já reflete a sessão persistida (lazy init
    // acima); aqui só resta disparar a renovação assíncrona quando o token guardado
    // já expirou mas ainda há refresh token. O fetch é feito inline (em vez de
    // delegar para `refresh`) para que o setState só aconteça dentro do callback
    // assíncrono, nunca no corpo síncrono do efeito.
    const stored = readStoredSession()
    if (!stored || stored.expiresAt > Date.now()) {
      return
    }
    if (!stored.refreshToken) {
      writeStoredSession(null)
      return
    }
    const authority = buildAuthority()
    if (!authority || !clientId) {
      writeStoredSession(null)
      return
    }
    fetch(`${authority}/protocol/openid-connect/token`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        grant_type: 'refresh_token',
        client_id: clientId,
        refresh_token: stored.refreshToken,
      }),
    })
      .then(async (response) => {
        if (!response.ok) {
          writeStoredSession(null)
          return
        }
        const next = toStoredSession((await response.json()) as TokenResponse)
        writeStoredSession(next)
        setSession(next)
      })
      .finally(() => setIsLoading(false))
    // executa apenas na montagem: recuperação de sessão persistida
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    setAccessTokenProvider(() => session?.accessToken ?? null)

    window.clearTimeout(refreshTimer.current)
    const refreshToken = session?.refreshToken
    if (session && refreshToken) {
      const delay = Math.max(session.expiresAt - Date.now() - REFRESH_MARGIN_MS, 0)
      refreshTimer.current = window.setTimeout(() => {
        void refresh(refreshToken)
      }, delay)
    }
    return () => window.clearTimeout(refreshTimer.current)
  }, [session, refresh])

  const login = useCallback(
    async (email: string, password: string) => {
      const authority = buildAuthority()
      if (!authority || !clientId) {
        throw new Error('Autenticação indisponível: Keycloak não configurado.')
      }
      const response = await fetch(`${authority}/protocol/openid-connect/token`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
          grant_type: 'password',
          client_id: clientId,
          username: email,
          password,
          scope: 'openid profile email',
        }),
      })
      if (!response.ok) {
        throw new Error('Credenciais inválidas. Verifique seu e-mail e senha.')
      }
      // Novo login pode trocar de usuário: descarta caches (ex.: `me`, `perfilAtivo`)
      // do usuário anterior para não vazar seu perfil/escopo para a nova sessão.
      queryClient.clear()
      applyTokenResponse((await response.json()) as TokenResponse)
    },
    [applyTokenResponse, clientId],
  )

  const logout = useCallback(() => {
    const authority = buildAuthority()
    const refreshToken = session?.refreshToken
    clearSession()
    queryClient.clear()
    if (authority && clientId && refreshToken) {
      void fetch(`${authority}/protocol/openid-connect/logout`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ client_id: clientId, refresh_token: refreshToken }),
      })
    }
  }, [session, clearSession, clientId])

  return {
    isAuthenticated: Boolean(session),
    isLoading,
    isConfigured,
    user: session ? toAuthUser(session.accessToken) : null,
    roles: session ? toRoles(session.accessToken) : [],
    login,
    logout,
  }
}
