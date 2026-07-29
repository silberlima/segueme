package com.silberlima.segueme.financeiro.api.dto;

import com.silberlima.segueme.financeiro.domain.TipoLancamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Registra um lançamento (receita ou despesa) de um Encontro (RF-047). */
public record RegistrarLancamentoRequest(
        @NotNull UUID encontroId,
        @NotNull TipoLancamento tipo,
        @NotBlank String categoria,
        @NotNull @Positive BigDecimal valor,
        @NotNull LocalDate data) {
}
