package com.silberlima.segueme.inscricoes.api.dto;

import java.time.Instant;
import java.util.UUID;

public record PreInscricaoResponse(
        UUID id,
        UUID pessoaId,
        String pessoaNome,
        int idade,
        String indicadoPorNome,
        UUID encontroId,
        String status,
        Instant recebidaEm) {
}
