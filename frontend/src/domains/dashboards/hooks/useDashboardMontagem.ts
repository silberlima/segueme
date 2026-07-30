import { useQuery } from '@tanstack/react-query'
import { OPCOES_DE_PAINEL } from './opcoesDePainel'
import { fetchDashboardMontagem } from '../services/dashboard.service'

export function useDashboardMontagem() {
  return useQuery({ queryKey: ['dashboards', 'montagem'], queryFn: fetchDashboardMontagem, ...OPCOES_DE_PAINEL })
}
