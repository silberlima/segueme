package com.silberlima.segueme.relatorios.domain;

import java.util.UUID;

/**
 * Publicado por {@code SolicitarRelatorioUseCase} e consumido depois do commit: é o que separa a
 * resposta ao usuário da geração propriamente dita (skills/10 — operação demorada é assíncrona,
 * nunca dentro da transação da request).
 */
public record RelatorioSolicitadoEvent(UUID relatorioId) {
}
