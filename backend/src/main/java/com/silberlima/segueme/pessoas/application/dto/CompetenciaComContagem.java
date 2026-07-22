package com.silberlima.segueme.pessoas.application.dto;

import com.silberlima.segueme.pessoas.domain.Competencia;

/** Resultado de {@code ListarCompetenciasUseCase} (RF-013): competência do catálogo + contagem de pessoas. */
public record CompetenciaComContagem(Competencia competencia, long numPessoas) {
}
