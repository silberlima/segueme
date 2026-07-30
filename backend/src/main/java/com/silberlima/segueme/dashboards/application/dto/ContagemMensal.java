package com.silberlima.segueme.dashboards.application.dto;

import java.time.YearMonth;

/** Barra do gráfico mensal "Frequência nos encontros pós" do Painel Pós-Encontro. */
public record ContagemMensal(YearMonth mes, long total) {
}
