package com.silberlima.segueme.dashboards.api.dto;

/** Barra do gráfico mensal de frequência do Painel Pós-Encontro; {@code mes} no formato {@code yyyy-MM}. */
public record ContagemMensalResponse(String mes, long total) {
}
