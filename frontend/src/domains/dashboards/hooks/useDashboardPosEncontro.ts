import { useQuery } from '@tanstack/react-query'
import { OPCOES_DE_PAINEL } from './opcoesDePainel'
import { fetchDashboardPosEncontro } from '../services/dashboard.service'

export function useDashboardPosEncontro() {
  return useQuery({ queryKey: ['dashboards', 'pos-encontro'], queryFn: fetchDashboardPosEncontro, ...OPCOES_DE_PAINEL })
}
