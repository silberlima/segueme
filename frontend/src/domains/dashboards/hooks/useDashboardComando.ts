import { useQuery } from '@tanstack/react-query'
import { OPCOES_DE_PAINEL } from './opcoesDePainel'
import { fetchDashboardComando } from '../services/dashboard.service'

export function useDashboardComando() {
  return useQuery({ queryKey: ['dashboards', 'comando'], queryFn: fetchDashboardComando, ...OPCOES_DE_PAINEL })
}
