package com.silberlima.segueme.pessoas.api.dto;

import java.time.Instant;
import java.util.UUID;

public record HistoricoEventoResponse(UUID id, String tipo, Instant dataEvento, String descricao) {
}
