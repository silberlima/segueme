package com.silberlima.segueme.dashboards.api.dto;

import java.time.LocalDate;
import java.util.UUID;

/** Linha da lista "Paróquias do setor"; {@code proximoEncontro} nulo = sem Encontro ativo em aberto. */
public record ParoquiaDoSetorResponse(UUID paroquiaId, String nome, long seguidores, LocalDate proximoEncontro) {
}
