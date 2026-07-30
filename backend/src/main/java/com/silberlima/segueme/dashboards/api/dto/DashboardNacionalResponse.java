package com.silberlima.segueme.dashboards.api.dto;

import java.util.List;

/** Painel Nacional (`dash_nacional`, RF-053). */
public record DashboardNacionalResponse(
        long seguidores,
        long dioceses,
        long paroquias,
        long encontrosNoAno,
        List<ContagemRegiaoResponse> encontrosPorRegiao,
        List<DioceseDestaqueResponse> diocesesDestaque) {
}
