package com.silberlima.segueme.movimentacao.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EmprestimoResponse(
        UUID id,
        UUID encontroId,
        int encontroNumero,
        UUID equipeId,
        String equipeNome,
        String paroquiaSolicitanteNome,
        UUID competenciaRequeridaId,
        String competenciaRequeridaNome,
        String status,
        String justificativaRecusa,
        Instant createdAt,
        List<EmprestimoCandidatoResponse> candidatos) {
}
