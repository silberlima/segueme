export type TipoPalestrante = 'EXTERNO' | 'SEGUIDOR' | 'CLERO'

export const TIPO_PALESTRANTE_LABEL: Record<TipoPalestrante, string> = {
  EXTERNO: 'Convidado externo',
  SEGUIDOR: 'Seguidor',
  CLERO: 'Clero',
}

/** Um dos 7 temas oficiais do encontro, em ordem fixa (RF-051, RN-CONTEUDO-01). */
export interface PalestraDoEncontro {
  palestraId: string
  numero: number
  tema: string
  palestranteId: string | null
  palestranteNome: string | null
  palestranteTipo: TipoPalestrante | null
}

/** Pessoa que ministra uma palestra do encontro (RF-052, RN-CONTEUDO-02). */
export interface Palestrante {
  id: string
  nome: string
  tipo: TipoPalestrante
  pessoaId: string | null
  createdAt: string
}

export interface CadastrarPalestrantePayload {
  tipo: TipoPalestrante
  nome: string | null
  pessoaId: string | null
}
