import { useQuery } from '@tanstack/react-query'
import { OPCOES_DE_PAINEL } from './opcoesDePainel'
import {
  fetchDashboardComando,
  fetchDashboardFichas,
  fetchDashboardMontagem,
  fetchDashboardParoquia,
  fetchDashboardSetorial,
} from '../services/dashboard.service'

/**
 * Contagens de pendência por rota do menu lateral (RN-DASH-02: o indicador de pendência do painel
 * também aparece como badge no menu correspondente).
 *
 * Só consulta o painel do perfil ativo, e reusa a mesma `queryKey` dos hooks de painel — abrir o
 * painel e ver o badge no menu não gera duas requisições. O perfil chega como string para este
 * domínio não depender do tipo `Perfil`, que pertence ao domínio de sistema.
 *
 * A rota badgeada é a que leva ao trabalho pendente; quando a tela de detalhe não é alcançável pelo
 * menu (as que exigem um Encontro na URL), o badge fica no próprio item do painel.
 */
export function useBadgesDoMenu(perfilAtivo: string | null): Record<string, number> {
  const setorial = useQuery({
    queryKey: ['dashboards', 'setorial'],
    queryFn: fetchDashboardSetorial,
    enabled: perfilAtivo === 'SETORIAL',
    ...OPCOES_DE_PAINEL,
  })
  const paroquia = useQuery({
    queryKey: ['dashboards', 'paroquia'],
    queryFn: fetchDashboardParoquia,
    enabled: perfilAtivo === 'PAROQUIA',
    ...OPCOES_DE_PAINEL,
  })
  const fichas = useQuery({
    queryKey: ['dashboards', 'fichas'],
    queryFn: fetchDashboardFichas,
    enabled: perfilAtivo === 'EQUIPE_FICHAS',
    ...OPCOES_DE_PAINEL,
  })
  const montagem = useQuery({
    queryKey: ['dashboards', 'montagem'],
    queryFn: fetchDashboardMontagem,
    enabled: perfilAtivo === 'EQUIPE_MONTAGEM',
    ...OPCOES_DE_PAINEL,
  })
  const comando = useQuery({
    queryKey: ['dashboards', 'comando'],
    queryFn: fetchDashboardComando,
    enabled: perfilAtivo === 'EQUIPE_COMANDO',
    ...OPCOES_DE_PAINEL,
  })

  const badges: Record<string, number> = {}

  if (setorial.data) {
    badges['/setorial/solicitacoes'] = setorial.data.solicitacoesPendentes
  }
  if (paroquia.data) {
    badges['/painel/paroquia'] = paroquia.data.totalPendencias
  }
  if (fichas.data) {
    badges['/painel/fichas'] = fichas.data.aguardandoFicha
  }
  if (montagem.data) {
    badges['/painel/montagem'] = montagem.data.equipesIncompletas
  }
  if (comando.data) {
    badges['/painel/comando'] = comando.data.pendentes
  }

  return badges
}
