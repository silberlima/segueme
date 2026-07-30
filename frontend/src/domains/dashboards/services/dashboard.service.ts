import { httpClient } from '../../../shared/utils/httpClient'
import type {
  DashboardComando,
  DashboardDiocese,
  DashboardFichas,
  DashboardFinancas,
  DashboardMontagem,
  DashboardNacional,
  DashboardParoquia,
  DashboardPosEncontro,
  DashboardSetorial,
} from '../domain/dashboard.types'

/**
 * Cada painel tem sua própria rota, restrita ao perfil correspondente no backend (RN-DASH-01).
 * Nenhuma delas recebe território ou Encontro: o escopo vem das claims do usuário autenticado.
 */

export async function fetchDashboardNacional(): Promise<DashboardNacional> {
  const { data } = await httpClient.get<DashboardNacional>('/dashboards/nacional')
  return data
}

export async function fetchDashboardDiocese(): Promise<DashboardDiocese> {
  const { data } = await httpClient.get<DashboardDiocese>('/dashboards/diocese')
  return data
}

export async function fetchDashboardSetorial(): Promise<DashboardSetorial> {
  const { data } = await httpClient.get<DashboardSetorial>('/dashboards/setorial')
  return data
}

export async function fetchDashboardParoquia(): Promise<DashboardParoquia> {
  const { data } = await httpClient.get<DashboardParoquia>('/dashboards/paroquia')
  return data
}

export async function fetchDashboardFichas(): Promise<DashboardFichas> {
  const { data } = await httpClient.get<DashboardFichas>('/dashboards/fichas')
  return data
}

export async function fetchDashboardMontagem(): Promise<DashboardMontagem> {
  const { data } = await httpClient.get<DashboardMontagem>('/dashboards/montagem')
  return data
}

export async function fetchDashboardComando(): Promise<DashboardComando> {
  const { data } = await httpClient.get<DashboardComando>('/dashboards/comando')
  return data
}

export async function fetchDashboardFinancas(): Promise<DashboardFinancas> {
  const { data } = await httpClient.get<DashboardFinancas>('/dashboards/financas')
  return data
}

export async function fetchDashboardPosEncontro(): Promise<DashboardPosEncontro> {
  const { data } = await httpClient.get<DashboardPosEncontro>('/dashboards/pos-encontro')
  return data
}
