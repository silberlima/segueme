package com.silberlima.segueme.posencontro.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

/** Agenda um encontro pós numa paróquia (RN-POS-01). {@code tema} é livre e opcional. */
public record AgendarEncontroPosRequest(
        @NotNull UUID paroquiaId, @NotNull LocalDate data, @Size(max = 200) String tema) {
}
