import { useQuery } from '@tanstack/react-query'
import { OPCOES_DE_PAINEL } from './opcoesDePainel'
import { fetchDashboardNacional } from '../services/dashboard.service'

export function useDashboardNacional() {
  return useQuery({ queryKey: ['dashboards', 'nacional'], queryFn: fetchDashboardNacional, ...OPCOES_DE_PAINEL })
}
