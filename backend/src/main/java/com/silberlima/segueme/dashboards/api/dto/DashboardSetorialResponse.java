package com.silberlima.segueme.dashboards.api.dto;

import com.silberlima.segueme.movimentacao.api.dto.SolicitacaoResponse;
import java.util.List;

/**
 * Painel do Setor (`dash_setorial`, RF-053). As solicitações usam o mesmo
 * {@code SolicitacaoResponse} do inbox {@code screen_solicitacoes}: aprovar/recusar a partir do
 * painel é a mesma ação central, com o mesmo contrato.
 */
public record DashboardSetorialResponse(
        long solicitacoesPendentes,
        long transferenciasNoMes,
        long emprestimosAtivos,
        long seguidores,
        List<SolicitacaoResponse> solicitacoes,
        List<ParoquiaDoSetorResponse> paroquias) {
}
