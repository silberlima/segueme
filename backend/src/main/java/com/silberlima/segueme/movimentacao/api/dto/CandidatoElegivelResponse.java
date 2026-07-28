package com.silberlima.segueme.movimentacao.api.dto;

import java.util.List;
import java.util.UUID;

/** Candidato do pool de elegíveis a um Empréstimo, para o Setorial filtrar e selecionar (RF-043). */
public record CandidatoElegivelResponse(
        UUID pessoaId, String pessoaNome, UUID paroquiaId, String paroquiaNome, List<String> competencias) {
}
