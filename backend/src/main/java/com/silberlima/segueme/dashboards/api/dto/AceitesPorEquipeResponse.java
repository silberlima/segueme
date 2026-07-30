package com.silberlima.segueme.dashboards.api.dto;

import java.util.UUID;

/** Barra do gráfico "Aceites por equipe" do Painel de Comando. */
public record AceitesPorEquipeResponse(UUID equipeId, String equipeNome, long aceitos, long convidados) {
}
