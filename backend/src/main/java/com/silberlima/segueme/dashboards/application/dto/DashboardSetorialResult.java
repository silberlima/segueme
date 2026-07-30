package com.silberlima.segueme.dashboards.application.dto;

import com.silberlima.segueme.movimentacao.application.dto.SolicitacaoItem;
import java.util.List;

/**
 * Painel do Setor (`dash_setorial`, RF-053). A lista de solicitações é a mesma de
 * {@code screen_solicitacoes} — reaproveita {@code ListarSolicitacoesUseCase} para que aprovar aqui
 * e aprovar no inbox sejam a mesma ação central (`dashboards-por-perfil.md` 3.3).
 */
public record DashboardSetorialResult(
        long solicitacoesPendentes,
        long transferenciasNoMes,
        long emprestimosAtivos,
        long seguidores,
        List<SolicitacaoItem> solicitacoes,
        List<ParoquiaDoSetor> paroquias) {
}
