package com.silberlima.segueme.hierarquia.application.dto;

import java.util.List;
import java.util.UUID;

public record DioceseArvoreResult(
        UUID id, String nome, int numParoquias, int numSeguidores, List<SetorArvoreResult> setores) {
}
