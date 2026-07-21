package com.silberlima.segueme.hierarquia.api.dto;

import java.util.List;
import java.util.UUID;

public record DioceseResponse(
        UUID id, String nome, int numParoquias, int numSeguidores, List<SetorResponse> setores) {
}
