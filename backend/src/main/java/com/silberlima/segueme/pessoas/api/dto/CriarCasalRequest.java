package com.silberlima.segueme.pessoas.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.UUID;

public record CriarCasalRequest(
        @NotNull UUID pessoa1Id, @NotNull UUID pessoa2Id, @NotNull @PastOrPresent LocalDate dataVinculo) {
}
