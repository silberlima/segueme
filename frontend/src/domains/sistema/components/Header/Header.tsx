import { useLocation } from 'react-router-dom'
import { useAuth } from '../../../../app/providers/AuthProvider'
import { useProfile } from '../../../../app/providers/ProfileProvider'
import { PERFIL_LABEL } from '../../domain/perfil.types'
import { CATALOGO_NAVEGACAO } from '../../domain/navegacao.catalogo'
import { GlobalSearch } from '../GlobalSearch'
import { NotificationBell } from '../NotificationBell'
import type { HeaderProps } from './Header.types'

export function Header({ className = '' }: HeaderProps) {
  const { user } = useAuth()
  const { perfilAtivo } = useProfile()
  const location = useLocation()

  const telaAtual = CATALOGO_NAVEGACAO.find((item) => item.path === location.pathname)?.titulo ?? 'Início'
  const iniciais = (user?.name ?? user?.email ?? '?').charAt(0).toUpperCase()

  return (
    <header className={`flex h-16 items-center justify-between border-b border-slate-200 bg-white px-6 ${className}`}>
      <p className="text-sm text-slate-500">
        {perfilAtivo && <span className="font-medium text-slate-700">{PERFIL_LABEL[perfilAtivo]}</span>}
        <span className="mx-2">›</span>
        {telaAtual}
      </p>

      <div className="flex items-center gap-3">
        <GlobalSearch />
        <NotificationBell />
        <div className="flex h-9 w-9 items-center justify-center rounded-full bg-violet-100 text-sm font-semibold text-violet-700">
          {iniciais}
        </div>
      </div>
    </header>
  )
}
