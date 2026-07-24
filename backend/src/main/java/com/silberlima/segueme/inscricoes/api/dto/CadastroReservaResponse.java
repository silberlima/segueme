package com.silberlima.segueme.inscricoes.api.dto;

import java.time.Instant;
import java.util.UUID;

public record CadastroReservaResponse(
        UUID id, int ordem, UUID pessoaId, String pessoaNome, String paroquiaNome, Instant naFilaDesde, String status) {
}
