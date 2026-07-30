import { httpClient } from '../../../shared/utils/httpClient'
import type { Pagination } from '../../../shared/types/pagination.types'
import type { ArquivoDeRelatorio, Relatorio, TipoDeRelatorio, TipoRelatorio } from '../domain/relatorio.types'

/** Já vem filtrado pelos perfis do usuário: o backend é a fonte única de quem gera o quê (RF-056). */
export async function fetchTiposDeRelatorio(): Promise<TipoDeRelatorio[]> {
  const { data } = await httpClient.get<TipoDeRelatorio[]>('/relatorios/tipos')
  return data
}

export async function fetchRelatorios(page = 0, size = 10): Promise<Pagination<Relatorio>> {
  const { data } = await httpClient.get<Pagination<Relatorio>>('/relatorios', { params: { page, size } })
  return data
}

/** Responde 202 com o relatório ainda pendente — a geração acontece fora da request. */
export async function solicitarRelatorio(tipo: TipoRelatorio): Promise<Relatorio> {
  const { data } = await httpClient.post<Relatorio>('/relatorios', { tipo })
  return data
}

export async function fetchArquivoDeRelatorio(id: string): Promise<ArquivoDeRelatorio> {
  const resposta = await httpClient.get<Blob>(`/relatorios/${id}/conteudo`, { responseType: 'blob' })
  return { conteudo: resposta.data, nomeArquivo: nomeDoArquivo(resposta.headers['content-disposition']) }
}

/** O nome do arquivo é decidido pelo backend e viaja no Content-Disposition. */
function nomeDoArquivo(contentDisposition: unknown): string {
  const cabecalho = typeof contentDisposition === 'string' ? contentDisposition : ''
  return /filename="([^"]+)"/.exec(cabecalho)?.[1] ?? 'relatorio.csv'
}
