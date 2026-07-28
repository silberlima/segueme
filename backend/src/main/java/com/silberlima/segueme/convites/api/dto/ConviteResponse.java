package com.silberlima.segueme.convites.api.dto;

import java.time.Instant;
import java.util.UUID;

/** Um convite (RF-036/037), representado pelo vínculo `IntegranteEquipe` que ele governa. */
public record ConviteResponse(
        UUID id,
        UUID encontroId,
        UUID equipeId,
        String equipeNome,
        UUID pessoaId,
        String pessoaNome,
        String papel,
        String status,
        Instant dataEnvioConvite,
        Instant dataResposta,
        String justificativaRecusa,
        int reenvios) {
}
