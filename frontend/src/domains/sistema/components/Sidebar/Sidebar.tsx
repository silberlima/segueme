import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../../../../app/providers/AuthProvider'
import { useBadges } from '../../../../app/providers/BadgeProvider'
import { useProfile } from '../../../../app/providers/ProfileProvider'
import { Badge, Select } from '../../../../shared/ui'
import { PERFIL_LABEL } from '../../domain/perfil.types'
import type { Perfil } from '../../domain/perfil.types'
import { getMenuParaPerfil } from '../../domain/navegacao.entity'
import type { SidebarProps } from './Sidebar.types'

export function Sidebar({ className = '' }: SidebarProps) {
  const { user, logout } = useAuth()
  const { perfis, perfilAtivo, setPerfilAtivo } = useProfile()
  const { badges } = useBadges()
  const location = useLocation()

  // Itens 'pronto' sem path (ex.: telas de detalhe só alcançáveis via id, como Perfil/Histórico
  // da Pessoa) não viram link estático no menu — só aparecem no Mapa de Navegação.
  const menu = getMenuParaPerfil(perfilAtivo).filter((item) => item.path !== null && item.path !== '/mapa')

  return (
    <aside className={`flex h-full w-64 flex-col border-r border-slate-200 bg-white ${className}`}>
      <div className="p-4">
        <p className="mb-4 text-lg font-semibold text-slate-900">Segue-me</p>
        {perfis.length > 0 && (
          <Select
            label="Perfil de acesso"
            value={perfilAtivo ?? ''}
            options={perfis.map((perfil) => ({ value: perfil, label: PERFIL_LABEL[perfil] }))}
            onChange={(event) => setPerfilAtivo(event.target.value as Perfil)}
          />
        )}
      </div>

      <nav className="flex-1 overflow-y-auto px-2">
        {menu.map((item) => {
          // Badge de pendência do painel do perfil ativo (RN-DASH-02).
          const pendencias = item.path ? (badges[item.path] ?? 0) : 0

          return (
            <Link
              key={item.titulo}
              to={item.path ?? '#'}
              className={`flex items-center justify-between gap-2 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                location.pathname === item.path
                  ? 'bg-violet-50 text-violet-700'
                  : 'text-slate-600 hover:bg-slate-50'
              }`}
            >
              <span>{item.titulo}</span>
              {pendencias > 0 && <Badge tone="warning">{pendencias}</Badge>}
            </Link>
          )
        })}
      </nav>

      <div className="border-t border-slate-200 p-4">
        <Link to="/mapa" className="mb-3 block text-sm font-medium text-violet-600 hover:underline">
          Mapa de navegação
        </Link>
        <div className="mb-3">
          <p className="text-sm font-medium text-slate-900">{user?.name ?? user?.email}</p>
          {perfilAtivo && <p className="text-xs text-slate-500">{PERFIL_LABEL[perfilAtivo]}</p>}
        </div>
        <button
          type="button"
          onClick={logout}
          className="text-sm font-medium text-rose-600 hover:underline"
        >
          Sair
        </button>
      </div>
    </aside>
  )
}
