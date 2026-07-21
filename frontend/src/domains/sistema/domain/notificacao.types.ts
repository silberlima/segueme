export type TipoNotificacao = 'INFORMATIVA' | 'POSITIVA' | 'NEGATIVA'

export interface Notificacao {
  id: string
  tipo: TipoNotificacao
  titulo: string
  mensagem: string
  lida: boolean
  createdAt: string
}
