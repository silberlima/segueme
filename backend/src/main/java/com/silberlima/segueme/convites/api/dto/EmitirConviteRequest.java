package com.silberlima.segueme.convites.api.dto;

import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Emite um convite de coordenador (RF-034) ou de integrante (RF-035) para uma equipe. */
public record EmitirConviteRequest(@NotNull UUID equipeId, @NotNull UUID pessoaId, @NotNull PapelIntegrante papel) {
}
