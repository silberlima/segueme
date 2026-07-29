package com.silberlima.segueme.financeiro.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record RegistrarArrecadacaoRequest(@NotNull @Positive BigDecimal valor) {
}
