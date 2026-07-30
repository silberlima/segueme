export type TipoNotificacao = 'INFORMATIVA' | 'POSITIVA' | 'NEGATIVA'

export interface Notificacao {
  id: string
  tipo: TipoNotificacao
  titulo: string
  mensagem: string
  lida: boolean
  createdAt: string
}

/** RF-057: um toggle por tipo de notificação, na seção Notificações de Configurações. */
export interface PreferenciaNotificacao {
  tipo: TipoNotificacao
  ativo: boolean
}

/** O que o usuário mexeu nos toggles desde a última gravação — só os tipos tocados. */
export type AjustesDePreferencia = Partial<Record<TipoNotificacao, boolean>>
