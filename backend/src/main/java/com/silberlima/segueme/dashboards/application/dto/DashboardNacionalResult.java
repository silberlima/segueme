package com.silberlima.segueme.dashboards.application.dto;

import java.util.List;

/**
 * Painel Nacional (`dash_nacional`, RF-053): seguidores no Brasil, dioceses, paróquias e Encontros
 * no ano, mais o gráfico por região e o ranking de dioceses em destaque.
 */
public record DashboardNacionalResult(
        long seguidores,
        long dioceses,
        long paroquias,
        long encontrosNoAno,
        List<ContagemRegiao> encontrosPorRegiao,
        List<DioceseDestaque> diocesesDestaque) {
}
