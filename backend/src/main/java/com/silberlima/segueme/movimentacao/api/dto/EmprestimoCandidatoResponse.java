package com.silberlima.segueme.movimentacao.api.dto;

import java.util.UUID;

public record EmprestimoCandidatoResponse(
        UUID id, UUID pessoaId, String pessoaNome, UUID paroquiaOrigemId, String paroquiaOrigemNome, String status) {
}
