package com.silberlima.segueme.dashboards.api.dto;

/** Uma pendência do Painel da Paróquia e seu badge no menu (RN-DASH-02); {@code tipo} vem de TipoPendencia. */
public record PendenciaResponse(String tipo, long quantidade) {
}
