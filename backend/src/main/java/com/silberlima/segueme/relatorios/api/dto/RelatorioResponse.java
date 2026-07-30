package com.silberlima.segueme.relatorios.api.dto;

import java.time.Instant;
import java.util.UUID;

/** Um item de "Gerados recentemente" — metadados apenas; o arquivo sai pelo endpoint de conteúdo. */
public record RelatorioResponse(
        UUID id,
        String tipo,
        String tipoTitulo,
        String status,
        Integer totalLinhas,
        String erro,
        Instant solicitadoEm,
        Instant geradoEm) {
}
