import { BadgeContext } from './BadgeContext'
import { useProfile } from '../ProfileProvider'
import { useBadgesDoMenu } from '../../../domains/dashboards/hooks/useBadgesDoMenu'
import type { BadgeProviderProps } from './BadgeProvider.types'

/**
 * Disponibiliza as contagens de pendência do painel do perfil ativo para o menu lateral
 * (RN-DASH-02). Vive na camada de aplicação porque cruza dois domínios: o menu (sistema) e os
 * painéis (dashboards), que não podem importar um do outro.
 */
export function BadgeProvider({ children }: BadgeProviderProps) {
  const { perfilAtivo } = useProfile()
  const badges = useBadgesDoMenu(perfilAtivo)

  return <BadgeContext.Provider value={{ badges }}>{children}</BadgeContext.Provider>
}
