package com.silberlima.segueme.financeiro.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CampanhaResponse(
        UUID id,
        UUID encontroId,
        int encontroNumero,
        String nome,
        BigDecimal meta,
        BigDecimal valorArrecadado,
        String status,
        Instant createdAt) {
}
