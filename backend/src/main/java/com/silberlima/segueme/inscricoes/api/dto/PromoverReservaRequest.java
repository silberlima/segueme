package com.silberlima.segueme.inscricoes.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PromoverReservaRequest(@NotNull UUID encontroDestinoId) {
}
