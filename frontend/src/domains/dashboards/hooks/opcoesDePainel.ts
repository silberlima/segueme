/**
 * Um painel é a visão de entrada do perfil: ao abri-lo, os KPIs têm que refletir o que já foi
 * registrado nas telas de detalhe (uma chamada salva, um lançamento novo). Por isso os painéis
 * sempre reconsultam ao montar, em vez de servir o cache de 60s do `queryClient` — o que também
 * evita exigir que toda mutação de todos os domínios invalide uma chave que não é dela.
 */
export const OPCOES_DE_PAINEL = {
  staleTime: 0,
  refetchOnMount: 'always',
} as const
