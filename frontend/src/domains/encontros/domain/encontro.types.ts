export type StatusEncontro = 'ATIVO' | 'ENCERRADO'

export const STATUS_ENCONTRO_LABEL: Record<StatusEncontro, string> = {
  ATIVO: 'Ativo',
  ENCERRADO: 'Encerrado',
}

export type PastaDirigente = 'FINANCAS' | 'FICHAS' | 'PALESTRAS' | 'POS_ENCONTRO' | 'MONTAGEM'

export const PASTAS_DIRIGENTE: PastaDirigente[] = ['FINANCAS', 'FICHAS', 'PALESTRAS', 'POS_ENCONTRO', 'MONTAGEM']

export const PASTA_DIRIGENTE_LABEL: Record<PastaDirigente, string> = {
  FINANCAS: 'Finanças',
  FICHAS: 'Fichas',
  PALESTRAS: 'Palestras',
  POS_ENCONTRO: 'Pós-Encontro',
  MONTAGEM: 'Montagem',
}

export type TipoMembroDirigente = 'JOVEM' | 'CASAL'

export interface EncontroResumo {
  id: string
  numero: number
  padroeiro: string
  status: StatusEncontro
  dataInicio: string
  dataTermino: string
  local: string
  paroquiaId: string
  paroquiaNome: string
  coordenadorGeralNome: string | null
  faseAtual: string
  numSeguidores: number
  numEquipes: number
  numEquipistas: number
}

export interface MembroDirigente {
  pasta: PastaDirigente
  tipoMembro: TipoMembroDirigente
  pessoaId: string | null
  pessoaNome: string | null
  casalId: string | null
  casalNome: string | null
}

export interface EquipeComandoInfo {
  coordenadorGeralId: string
  coordenadorGeralNome: string
  casalCoordenadorId: string
  casalCoordenadorNome: string
}

export interface Encontro {
  id: string
  numero: number
  padroeiro: string
  status: StatusEncontro
  diretorEspiritualId: string
  diretorEspiritualNome: string
  local: string
  dataInicio: string
  dataTermino: string
  vagas: number
  paroquiaId: string
  paroquiaNome: string
  equipeDirigente: MembroDirigente[]
  equipeComando: EquipeComandoInfo
  createdAt: string
}

export interface MembroDirigenteInputPayload {
  pasta: PastaDirigente
  tipoMembro: TipoMembroDirigente
  pessoaId?: string
  casalId?: string
}

export interface NovoEncontroPayload {
  padroeiro: string
  diretorEspiritualId: string
  local: string
  dataInicio: string
  dataTermino: string
  vagas: number
  paroquiaId: string
  equipeDirigente: MembroDirigenteInputPayload[]
  equipeComando: {
    coordenadorGeralId: string
    casalCoordenadorId: string
  }
}
