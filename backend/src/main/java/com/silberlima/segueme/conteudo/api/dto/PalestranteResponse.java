package com.silberlima.segueme.conteudo.api.dto;

import java.time.Instant;
import java.util.UUID;

public record PalestranteResponse(UUID id, String nome, String tipo, UUID pessoaId, Instant createdAt) {
}
