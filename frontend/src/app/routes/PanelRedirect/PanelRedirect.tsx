import { Navigate } from 'react-router-dom'
import { useProfile } from '../../providers/ProfileProvider'
import { getPainelDoPerfil } from '../../../domains/sistema/domain/painel.entity'
import { Skeleton } from '../../../shared/ui'

/**
 * Entrada no sistema: leva ao painel do perfil ativo (RF-053, RN-DASH-01). Enquanto os perfis do
 * usuário não carregam, mostra o esqueleto; perfil sem painel próprio (Seguidor) cai no Mapa de
 * Navegação, que é o índice de telas permitido a ele.
 */
export function PanelRedirect() {
  const { isLoading, perfilAtivo } = useProfile()

  if (isLoading) {
    return (
      <div className="p-6">
        <Skeleton variant="rect" height="120px" />
      </div>
    )
  }

  const painel = getPainelDoPerfil(perfilAtivo)

  return <Navigate to={painel ?? '/mapa'} replace />
}
