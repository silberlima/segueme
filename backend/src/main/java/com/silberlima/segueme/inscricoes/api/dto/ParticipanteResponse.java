package com.silberlima.segueme.inscricoes.api.dto;

import java.util.UUID;

public record ParticipanteResponse(
        UUID fichaInscricaoId, UUID pessoaId, String pessoaNome, String paroquiaNome, String statusPagamento) {
}
