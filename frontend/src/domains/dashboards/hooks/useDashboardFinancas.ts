import { useQuery } from '@tanstack/react-query'
import { OPCOES_DE_PAINEL } from './opcoesDePainel'
import { fetchDashboardFinancas } from '../services/dashboard.service'

export function useDashboardFinancas() {
  return useQuery({ queryKey: ['dashboards', 'financas'], queryFn: fetchDashboardFinancas, ...OPCOES_DE_PAINEL })
}
