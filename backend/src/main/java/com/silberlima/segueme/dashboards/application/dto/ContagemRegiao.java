package com.silberlima.segueme.dashboards.application.dto;

import com.silberlima.segueme.hierarquia.domain.RegiaoBrasil;

/**
 * Uma barra do gráfico "Encontros por região" do Painel Nacional. {@code regiao} nula agrupa os
 * Encontros de dioceses sem região informada (ver {@link RegiaoBrasil}).
 */
public record ContagemRegiao(RegiaoBrasil regiao, long total) {
}
