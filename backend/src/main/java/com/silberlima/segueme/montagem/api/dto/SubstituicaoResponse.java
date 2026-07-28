package com.silberlima.segueme.montagem.api.dto;

import java.time.Instant;
import java.util.UUID;

/** Item do histórico de substituições de uma equipe (RF-033). */
public record SubstituicaoResponse(
        UUID id,
        String equipeNome,
        UUID pessoaSaiId,
        String pessoaSaiNome,
        String motivoSaida,
        UUID pessoaEntraId,
        String pessoaEntraNome,
        String justificativa,
        Instant dataSubstituicao) {
}
