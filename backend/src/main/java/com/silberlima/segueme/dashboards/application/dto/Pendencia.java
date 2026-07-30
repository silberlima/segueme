package com.silberlima.segueme.dashboards.application.dto;

import com.silberlima.segueme.dashboards.domain.TipoPendencia;

/** Uma pendência do Painel da Paróquia e seu badge correspondente no menu (RN-DASH-02). */
public record Pendencia(TipoPendencia tipo, long quantidade) {
}
