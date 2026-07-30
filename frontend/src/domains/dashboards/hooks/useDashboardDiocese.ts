import { useQuery } from '@tanstack/react-query'
import { OPCOES_DE_PAINEL } from './opcoesDePainel'
import { fetchDashboardDiocese } from '../services/dashboard.service'

export function useDashboardDiocese() {
  return useQuery({ queryKey: ['dashboards', 'diocese'], queryFn: fetchDashboardDiocese, ...OPCOES_DE_PAINEL })
}
