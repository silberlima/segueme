package com.silberlima.segueme.encontros.api.dto;

import java.util.UUID;

public record MembroDirigenteResponse(
        String pasta, String tipoMembro, UUID pessoaId, String pessoaNome, UUID casalId, String casalNome) {
}
