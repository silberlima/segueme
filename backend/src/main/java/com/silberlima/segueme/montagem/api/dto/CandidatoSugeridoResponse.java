package com.silberlima.segueme.montagem.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * Um candidato ranqueado na tela de Sugestões Inteligentes (RF-029/030), exclusiva da Equipe
 * Montagem (RN-08/RN-MONTAGEM-02). {@code scoreVersao} identifica a fórmula usada — ver
 * {@link com.silberlima.segueme.montagem.domain.SuggestionEngine#VERSAO}.
 */
public record CandidatoSugeridoResponse(
        UUID pessoaId,
        String pessoaNome,
        int idade,
        String situacao,
        String paroquiaNome,
        boolean mesmaParoquia,
        boolean requerEmprestimoSetor,
        int scoreTotal,
        String scoreVersao,
        ScoreBreakdown breakdown,
        List<String> justificativas) {

    public record ScoreBreakdown(
            int historicoEquipesSemelhantes, int competencias, int frequenciaPosEncontro, int preRequisito) {
    }
}
