package com.silberlima.segueme.montagem.api.dto;

import java.util.UUID;

/** Um integrante (ou coordenador) de uma equipe de serviço, com seu status de vínculo (RF-028). */
public record IntegranteResponse(UUID id, UUID pessoaId, String pessoaNome, String papel, String status) {
}
