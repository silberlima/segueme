import type { BadgeTone } from '../../../shared/ui'
import type { AjustesDePreferencia, PreferenciaNotificacao, TipoNotificacao } from './notificacao.types'

/** RN-SISTEMA-01: informativa, positiva/ok e negativa/danger — o mesmo código de cor no drawer e na tela cheia. */
export const TONE_POR_TIPO: Record<TipoNotificacao, BadgeTone> = {
  INFORMATIVA: 'info',
  POSITIVA: 'success',
  NEGATIVA: 'danger',
}

export const LABEL_POR_TIPO: Record<TipoNotificacao, string> = {
  INFORMATIVA: 'Informativa',
  POSITIVA: 'Positiva',
  NEGATIVA: 'Negativa',
}

/** Descrição de cada toggle em Configurações — o usuário decide por efeito, não por nome técnico. */
export const DESCRICAO_POR_TIPO: Record<TipoNotificacao, string> = {
  INFORMATIVA: 'Avisos gerais do sistema, como convites enviados.',
  POSITIVA: 'Confirmações, como convites aceitos e relatórios prontos.',
  NEGATIVA: 'Recusas e falhas, com a justificativa quando houver.',
}

/** As preferências como a tela deve exibi-las: o que veio do servidor com o que o usuário mexeu por cima. */
export function aplicarAjustes(
  persistidas: PreferenciaNotificacao[],
  ajustes: AjustesDePreferencia,
): PreferenciaNotificacao[] {
  return persistidas.map((preferencia) => ({
    ...preferencia,
    ativo: ajustes[preferencia.tipo] ?? preferencia.ativo,
  }))
}

/** Reativar um toggle que já estava ativo não é alteração — o botão Salvar não deve acender por isso. */
export function temAjusteEfetivo(
  persistidas: PreferenciaNotificacao[],
  ajustes: AjustesDePreferencia,
): boolean {
  return persistidas.some((preferencia) => {
    const ajuste = ajustes[preferencia.tipo]
    return ajuste !== undefined && ajuste !== preferencia.ativo
  })
}

const UM_MINUTO = 60_000
const UMA_HORA = 60 * UM_MINUTO
const UM_DIA = 24 * UMA_HORA

/** Tempo relativo do item da central ("há 5 min"), como no protótipo. */
export function tempoRelativo(iso: string, agora: Date = new Date()): string {
  const decorrido = agora.getTime() - new Date(iso).getTime()
  if (decorrido < UM_MINUTO) return 'agora'
  if (decorrido < UMA_HORA) return `há ${Math.floor(decorrido / UM_MINUTO)} min`
  if (decorrido < UM_DIA) return `há ${Math.floor(decorrido / UMA_HORA)} h`
  return `há ${Math.floor(decorrido / UM_DIA)} d`
}
