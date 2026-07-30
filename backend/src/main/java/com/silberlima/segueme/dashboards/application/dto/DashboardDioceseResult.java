package com.silberlima.segueme.dashboards.application.dto;

import java.util.List;

/**
 * Painel da Diocese (`dash_diocese`, RF-053). {@code solicitacoesAoSetorial} soma transferências
 * pendentes e empréstimos em aberto da diocese — o mesmo dado que o Setorial decide no inbox
 * (`transferencias-e-emprestimos.md` 2.3), aqui apenas contado.
 */
public record DashboardDioceseResult(
        long seguidores,
        long setores,
        long paroquias,
        long encontrosNoAno,
        List<ContagemTerritorio> seguidoresPorSetor,
        long solicitacoesAoSetorial) {
}
