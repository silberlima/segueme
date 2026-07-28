package com.silberlima.segueme.movimentacao.api.dto;

import java.time.Instant;
import java.util.UUID;

public record TransferenciaResponse(
        UUID id,
        UUID pessoaId,
        String pessoaNome,
        UUID paroquiaOrigemId,
        String paroquiaOrigemNome,
        UUID paroquiaDestinoId,
        String paroquiaDestinoNome,
        String status,
        String motivo,
        String justificativaRecusa,
        Instant createdAt) {
}
