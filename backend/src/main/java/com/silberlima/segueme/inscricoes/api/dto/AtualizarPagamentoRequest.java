package com.silberlima.segueme.inscricoes.api.dto;

import com.silberlima.segueme.inscricoes.domain.StatusPagamento;
import jakarta.validation.constraints.NotNull;

public record AtualizarPagamentoRequest(@NotNull StatusPagamento statusPagamento) {
}
