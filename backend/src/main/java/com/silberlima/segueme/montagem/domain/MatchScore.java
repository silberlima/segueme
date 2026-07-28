package com.silberlima.segueme.montagem.domain;

/**
 * Resultado do {@link SuggestionEngine} (RF-030): score 0–100 e a quebra por fator, cada um já
 * na escala 0–100 antes de aplicar o peso. {@code versao} identifica a fórmula usada
 * (pendência #6 de {@code pendencias-gerais.md}: os pesos são fixos por RN-09/RN-MONTAGEM-03,
 * mas a fórmula interna de cada fator é melhor esforço e pode ser recalibrada sem quebrar o
 * contrato do endpoint, bastando revisar este identificador).
 */
public record MatchScore(
        int total,
        int historicoEquipesSemelhantes,
        int competencias,
        int frequenciaPosEncontro,
        int preRequisito,
        String versao) {
}
