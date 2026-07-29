package com.silberlima.segueme.posencontro.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record EncontroPosResponse(
        UUID id, UUID paroquiaId, String paroquiaNome, LocalDate data, String tema, Instant createdAt) {
}
