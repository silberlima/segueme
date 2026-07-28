package com.silberlima.segueme.montagem.application.dto;

import com.silberlima.segueme.montagem.domain.MatchScore;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.util.List;

/**
 * Um candidato ranqueado de {@code GerarSugestoesUseCase} (RF-029): a pessoa, o score calculado
 * pelo {@code SuggestionEngine} e as tags de justificativa exibidas na tela (RF-030,
 * "Cumpre pré-requisito", "N encontros na equipe", "Requer empréstimo do setor").
 */
public record CandidatoSugerido(Pessoa pessoa, MatchScore score, boolean mesmaParoquia, List<String> justificativas) {
}
