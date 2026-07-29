package com.silberlima.segueme.conteudo.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record DesignarPalestranteRequest(@NotNull UUID palestranteId) {
}
