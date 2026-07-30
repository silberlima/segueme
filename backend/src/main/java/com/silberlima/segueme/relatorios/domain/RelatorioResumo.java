package com.silberlima.segueme.relatorios.domain;

import java.time.Instant;
import java.util.UUID;

/** Projeção de leitura da lista "Gerados recentemente" — tudo menos o conteúdo do arquivo. */
public record RelatorioResumo(
        UUID id,
        TipoRelatorio tipo,
        StatusRelatorio status,
        Integer totalLinhas,
        String erro,
        Instant solicitadoEm,
        Instant geradoEm) {
}
