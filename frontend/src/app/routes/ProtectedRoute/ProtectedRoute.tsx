import { useAuth } from '../../providers/AuthProvider'
import { Skeleton } from '../../../shared/ui/Skeleton'
import { Button } from '../../../shared/ui/Button'
import type { ProtectedRouteProps } from './ProtectedRoute.types'

export function ProtectedRoute({ roles, children }: ProtectedRouteProps) {
  const { isAuthenticated, isLoading, roles: userRoles, login } = useAuth()

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Skeleton variant="rect" width="16rem" height="6rem" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return (
      <div className="flex h-screen flex-col items-center justify-center gap-4">
        <p className="text-sm text-slate-600">É necessário autenticar para continuar.</p>
        <Button onClick={login}>Entrar</Button>
      </div>
    )
  }

  const hasRequiredRole = !roles || roles.some((role) => userRoles.includes(role))

  if (!hasRequiredRole) {
    return (
      <div className="flex h-screen items-center justify-center">
        <p className="text-sm text-slate-600">Você não tem permissão para acessar esta página.</p>
      </div>
    )
  }

  return <>{children}</>
}
