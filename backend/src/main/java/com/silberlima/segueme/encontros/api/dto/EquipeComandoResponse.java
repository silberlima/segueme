package com.silberlima.segueme.encontros.api.dto;

import java.util.UUID;

public record EquipeComandoResponse(
        UUID coordenadorGeralId, String coordenadorGeralNome, UUID casalCoordenadorId, String casalCoordenadorNome) {
}
