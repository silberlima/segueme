import { QueryClientProvider } from '@tanstack/react-query'
import { queryClient } from './queryClient'
import type { QueryProviderProps } from './QueryProvider.types'

export function QueryProvider({ children }: QueryProviderProps) {
  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
}
