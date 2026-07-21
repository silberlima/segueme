export type BuscaTipo = 'PESSOA' | 'ENCONTRO'

export interface BuscaResultado {
  tipo: BuscaTipo
  id: string
  titulo: string
  subtitulo: string
}
