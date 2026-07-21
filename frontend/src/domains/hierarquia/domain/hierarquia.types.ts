export interface ParoquiaResumo {
  id: string
  nome: string
}

export interface Setor {
  id: string
  nome: string
  numParoquias: number
  numSeguidores: number
  paroquias: ParoquiaResumo[]
}

export interface Diocese {
  id: string
  nome: string
  numParoquias: number
  numSeguidores: number
  setores: Setor[]
}

export interface Paroquia {
  id: string
  nome: string
  setorId: string
  setorNome: string
  dioceseNome: string
  numSeguidores: number
  statusEncontroAtual: string | null
}
