import { useAuth } from '../../providers/AuthProvider'

export function RootLayout() {
  const { user } = useAuth()

  return (
    <div className="flex h-screen flex-col items-center justify-center gap-2">
      <h1 className="text-2xl font-semibold text-slate-900">Segue-me</h1>
      <p className="text-sm text-slate-500">{user?.name ?? user?.email ?? 'Bem-vindo(a).'}</p>
    </div>
  )
}
