import type { BadgeTone } from '../../../shared/ui'
import type { Relatorio, StatusRelatorio } from './relatorio.types'

export const STATUS_LABEL: Record<StatusRelatorio, string> = {
  PENDENTE: 'Gerando...',
  CONCLUIDO: 'Pronto',
  FALHOU: 'Falhou',
}

export const STATUS_TONE: Record<StatusRelatorio, BadgeTone> = {
  PENDENTE: 'warning',
  CONCLUIDO: 'success',
  FALHOU: 'danger',
}

export function podeBaixar(relatorio: Relatorio): boolean {
  return relatorio.status === 'CONCLUIDO'
}

/** Enquanto houver relatório em geração, a lista precisa se reconsultar sozinha. */
export function temRelatorioEmGeracao(relatorios: Relatorio[]): boolean {
  return relatorios.some((relatorio) => relatorio.status === 'PENDENTE')
}

const formatadorDataHora = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })

export function formatarDataHora(iso: string | null): string {
  return iso ? formatadorDataHora.format(new Date(iso)) : '—'
}
