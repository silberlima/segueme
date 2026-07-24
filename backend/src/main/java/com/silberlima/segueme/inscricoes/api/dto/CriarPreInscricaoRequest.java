package com.silberlima.segueme.inscricoes.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CriarPreInscricaoRequest(@NotNull UUID pessoaId, @NotNull UUID encontroId) {
}
