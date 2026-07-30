import { useQuery } from '@tanstack/react-query'
import { OPCOES_DE_PAINEL } from './opcoesDePainel'
import { fetchDashboardParoquia } from '../services/dashboard.service'

export function useDashboardParoquia() {
  return useQuery({ queryKey: ['dashboards', 'paroquia'], queryFn: fetchDashboardParoquia, ...OPCOES_DE_PAINEL })
}
