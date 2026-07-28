package com.silberlima.segueme.movimentacao.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Solicita a Transferência definitiva de uma pessoa para outra paróquia (RF-040). */
public record SolicitarTransferenciaRequest(@NotNull UUID pessoaId, @NotNull UUID paroquiaDestinoId, String motivo) {
}
