import { Outlet } from 'react-router-dom'
import { BadgeProvider } from '../../providers/BadgeProvider'
import { ProfileProvider } from '../../providers/ProfileProvider'
import { Sidebar } from '../../../domains/sistema/components/Sidebar'
import { Header } from '../../../domains/sistema/components/Header'

export function RootLayout() {
  return (
    <ProfileProvider>
      <BadgeProvider>
        <div className="flex h-screen bg-slate-50">
          <Sidebar />
          <div className="flex flex-1 flex-col overflow-hidden">
            <Header />
            <main className="flex-1 overflow-y-auto bg-slate-50">
              <Outlet />
            </main>
          </div>
        </div>
      </BadgeProvider>
    </ProfileProvider>
  )
}
