package com.silberlima.segueme.hierarquia.api.dto;

import java.util.UUID;

public record ParoquiaResponse(
        UUID id,
        String nome,
        UUID setorId,
        String setorNome,
        String dioceseNome,
        int numSeguidores,
        String statusEncontroAtual) {
}
