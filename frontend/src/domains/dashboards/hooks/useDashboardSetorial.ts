import { useQuery } from '@tanstack/react-query'
import { OPCOES_DE_PAINEL } from './opcoesDePainel'
import { fetchDashboardSetorial } from '../services/dashboard.service'

export function useDashboardSetorial() {
  return useQuery({ queryKey: ['dashboards', 'setorial'], queryFn: fetchDashboardSetorial, ...OPCOES_DE_PAINEL })
}
