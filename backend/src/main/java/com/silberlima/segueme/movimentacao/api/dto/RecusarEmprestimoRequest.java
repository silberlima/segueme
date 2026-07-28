package com.silberlima.segueme.movimentacao.api.dto;

import jakarta.validation.constraints.NotBlank;

/** Recusa de empréstimo pelo Setorial: justificativa obrigatória (mesmo padrão de RN-CONVITE-02). */
public record RecusarEmprestimoRequest(@NotBlank String justificativa) {
}
