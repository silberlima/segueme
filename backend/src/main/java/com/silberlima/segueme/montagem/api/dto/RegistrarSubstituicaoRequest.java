package com.silberlima.segueme.montagem.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Registra a substituição de um integrante por outro numa equipe (RF-032). Justificativa obrigatória (RN-MONTAGEM-05). */
public record RegistrarSubstituicaoRequest(
        @NotNull UUID encontroId,
        @NotNull UUID equipeId,
        @NotNull UUID pessoaSaiId,
        @NotBlank String motivoSaida,
        @NotNull UUID pessoaEntraId,
        @NotBlank String justificativa) {
}
