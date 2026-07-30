package com.silberlima.segueme.dashboards.api.dto;

import java.util.List;

/** Painel da Diocese (`dash_diocese`, RF-053). */
public record DashboardDioceseResponse(
        long seguidores,
        long setores,
        long paroquias,
        long encontrosNoAno,
        List<ContagemTerritorioResponse> seguidoresPorSetor,
        long solicitacoesAoSetorial) {
}
