package com.silberlima.segueme.financeiro.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

/** Cria uma campanha de arrecadação de um Encontro (RF-050). */
public record CriarCampanhaRequest(@NotNull UUID encontroId, @NotBlank String nome, @NotNull @Positive BigDecimal meta) {
}
