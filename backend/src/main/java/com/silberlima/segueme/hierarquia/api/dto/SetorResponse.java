package com.silberlima.segueme.hierarquia.api.dto;

import java.util.List;
import java.util.UUID;

public record SetorResponse(
        UUID id, String nome, int numParoquias, int numSeguidores, List<ParoquiaResumoResponse> paroquias) {
}
