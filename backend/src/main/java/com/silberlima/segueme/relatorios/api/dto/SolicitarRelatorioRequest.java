package com.silberlima.segueme.relatorios.api.dto;

import com.silberlima.segueme.relatorios.domain.TipoRelatorio;
import jakarta.validation.constraints.NotNull;

/** Pedido de geração de relatório (UC-22): o tipo escolhido no cartão da tela. */
public record SolicitarRelatorioRequest(@NotNull TipoRelatorio tipo) {
}
