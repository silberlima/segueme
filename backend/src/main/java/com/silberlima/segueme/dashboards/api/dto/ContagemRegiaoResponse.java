package com.silberlima.segueme.dashboards.api.dto;

/**
 * Barra do gráfico "Encontros por região" do Painel Nacional. {@code regiao} nula agrupa os
 * Encontros de dioceses sem região informada — o rótulo dessa faixa é responsabilidade do frontend.
 */
public record ContagemRegiaoResponse(String regiao, long total) {
}
