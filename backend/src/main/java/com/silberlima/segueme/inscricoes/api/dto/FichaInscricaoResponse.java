package com.silberlima.segueme.inscricoes.api.dto;

import java.time.Instant;
import java.util.UUID;

public record FichaInscricaoResponse(
        UUID id,
        UUID preInscricaoId,
        UUID pessoaId,
        String pessoaNome,
        UUID encontroId,
        String statusPagamento,
        Instant createdAt) {
}
