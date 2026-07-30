import { useQuery } from '@tanstack/react-query'
import { OPCOES_DE_PAINEL } from './opcoesDePainel'
import { fetchDashboardFichas } from '../services/dashboard.service'

export function useDashboardFichas() {
  return useQuery({ queryKey: ['dashboards', 'fichas'], queryFn: fetchDashboardFichas, ...OPCOES_DE_PAINEL })
}
