export type TipoRelatorio = 'PESSOAS' | 'ENCONTROS' | 'INSCRICOES' | 'EQUIPES' | 'FINANCEIRO' | 'POS_ENCONTRO'

/** RF-056: a geração é assíncrona, então o relatório nasce pendente e a tela acompanha o desfecho. */
export type StatusRelatorio = 'PENDENTE' | 'CONCLUIDO' | 'FALHOU'

/** Cartão de tipo de relatório; a lista vem do backend, já filtrada pelos perfis do usuário. */
export interface TipoDeRelatorio {
  tipo: TipoRelatorio
  titulo: string
  descricao: string
}

export interface Relatorio {
  id: string
  tipo: TipoRelatorio
  tipoTitulo: string
  status: StatusRelatorio
  totalLinhas: number | null
  erro: string | null
  solicitadoEm: string
  geradoEm: string | null
}

export interface ArquivoDeRelatorio {
  conteudo: Blob
  nomeArquivo: string
}
