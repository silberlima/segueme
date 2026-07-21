export type Perfil =
  | 'NACIONAL'
  | 'DIOCESE'
  | 'SETORIAL'
  | 'PAROQUIA'
  | 'EQUIPE_FICHAS'
  | 'EQUIPE_MONTAGEM'
  | 'EQUIPE_COMANDO'
  | 'EQUIPE_FINANCAS'
  | 'POS_ENCONTRO'
  | 'SEGUIDOR'

export const PERFIL_LABEL: Record<Perfil, string> = {
  NACIONAL: 'Nacional',
  DIOCESE: 'Diocese',
  SETORIAL: 'Setorial',
  PAROQUIA: 'Paróquia',
  EQUIPE_FICHAS: 'Equipe Fichas',
  EQUIPE_MONTAGEM: 'Equipe Montagem',
  EQUIPE_COMANDO: 'Equipe Comando',
  EQUIPE_FINANCAS: 'Equipe Finanças',
  POS_ENCONTRO: 'Pós-Encontro',
  SEGUIDOR: 'Seguidor',
}
