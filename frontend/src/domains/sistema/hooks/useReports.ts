import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { baixarArquivo } from '../../../shared/utils/baixarArquivo'
import { temRelatorioEmGeracao } from '../domain/relatorio.entity'
import {
  fetchArquivoDeRelatorio,
  fetchRelatorios,
  fetchTiposDeRelatorio,
  solicitarRelatorio,
} from '../services/relatorio.service'

const INTERVALO_DE_ACOMPANHAMENTO_MS = 2_000

/**
 * Tela Relatórios (RF-056, UC-22). Como a geração é assíncrona, "Gerados recentemente" se
 * reconsulta enquanto houver relatório pendente e para sozinha quando todos terminam — sem isso o
 * usuário teria que recarregar a página para descobrir que o arquivo ficou pronto.
 */
export function useReports() {
  const queryClient = useQueryClient()

  const tiposQuery = useQuery({ queryKey: ['relatorios', 'tipos'], queryFn: fetchTiposDeRelatorio })

  const relatoriosQuery = useQuery({
    queryKey: ['relatorios', 'lista'],
    queryFn: () => fetchRelatorios(0, 10),
    refetchInterval: (query) =>
      temRelatorioEmGeracao(query.state.data?.content ?? []) ? INTERVALO_DE_ACOMPANHAMENTO_MS : false,
  })

  const gerarMutation = useMutation({
    mutationFn: solicitarRelatorio,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['relatorios', 'lista'] })
      // A geração termina notificando o solicitante (RN-SISTEMA-01).
      queryClient.invalidateQueries({ queryKey: ['notificacoes'] })
    },
  })

  const baixarMutation = useMutation({
    mutationFn: fetchArquivoDeRelatorio,
    onSuccess: (arquivo) => baixarArquivo(arquivo.conteudo, arquivo.nomeArquivo),
  })

  return {
    tipos: tiposQuery.data ?? [],
    isLoadingTipos: tiposQuery.isLoading,
    relatorios: relatoriosQuery.data?.content ?? [],
    isLoadingRelatorios: relatoriosQuery.isLoading,
    isErrorRelatorios: relatoriosQuery.isError,
    recarregarRelatorios: relatoriosQuery.refetch,
    gerar: gerarMutation.mutate,
    tipoEmGeracao: gerarMutation.isPending ? gerarMutation.variables : null,
    baixar: baixarMutation.mutate,
    idEmDownload: baixarMutation.isPending ? baixarMutation.variables : null,
  }
}
