package com.silberlima.segueme.posencontro.api.dto;

import java.util.UUID;

/** Linha da lista de chamada. {@code presente} nulo = presença ainda não conferida. */
public record PresencaResponse(UUID pessoaId, String pessoaNome, String situacao, Boolean presente) {
}
