import axios from 'axios'
import type { AxiosInstance } from 'axios'

type AccessTokenProvider = () => string | null | undefined

let accessTokenProvider: AccessTokenProvider = () => null

export function setAccessTokenProvider(provider: AccessTokenProvider): void {
  accessTokenProvider = provider
}

export const httpClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
})

httpClient.interceptors.request.use((config) => {
  const token = accessTokenProvider()
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})
