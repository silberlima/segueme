package com.silberlima.segueme.montagem.api.dto;

import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Adiciona um candidato (sugerido ou não) a uma equipe de serviço (RF-031). */
public record AdicionarIntegranteRequest(@NotNull UUID pessoaId, @NotNull PapelIntegrante papel) {
}
