package com.silberlima.segueme.movimentacao.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Solicita um Empréstimo de seguidor de outra paróquia para uma vaga de equipe (RF-042). */
public record SolicitarEmprestimoRequest(@NotNull UUID encontroId, @NotNull UUID equipeId, UUID competenciaRequeridaId) {
}
