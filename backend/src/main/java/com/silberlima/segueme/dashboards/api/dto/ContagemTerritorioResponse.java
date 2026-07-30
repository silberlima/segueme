package com.silberlima.segueme.dashboards.api.dto;

import java.util.UUID;

/** Barra de gráfico ou linha de ranking por território (setor, paróquia, diocese). */
public record ContagemTerritorioResponse(UUID territorioId, String nome, long total) {
}
