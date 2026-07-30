package com.silberlima.segueme.dashboards.application.dto;

import java.util.UUID;

/**
 * Uma barra de gráfico ou linha de ranking já resolvida para o nome do território (diocese, setor
 * ou paróquia). O id acompanha o rótulo porque as listas dos painéis levam ao detalhe do território.
 */
public record ContagemTerritorio(UUID territorioId, String nome, long total) {
}
