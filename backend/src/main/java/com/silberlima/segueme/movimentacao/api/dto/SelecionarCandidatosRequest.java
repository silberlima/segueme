package com.silberlima.segueme.movimentacao.api.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

/** Seleção de candidatos do Setorial, enviada à Montagem (RF-043/044, RN-MOVIMENTO-05). */
public record SelecionarCandidatosRequest(@NotEmpty List<UUID> pessoaIds) {
}
