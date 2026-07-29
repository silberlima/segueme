package com.silberlima.segueme.financeiro.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LancamentoFinanceiroResponse(
        UUID id,
        UUID encontroId,
        int encontroNumero,
        String tipo,
        String categoria,
        BigDecimal valor,
        LocalDate data,
        String situacao,
        String aprovadoPor,
        Instant createdAt) {
}
