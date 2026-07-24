package com.silberlima.segueme.encontros.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EncontroResponse(
        UUID id,
        int numero,
        String padroeiro,
        String status,
        UUID diretorEspiritualId,
        String diretorEspiritualNome,
        String local,
        LocalDate dataInicio,
        LocalDate dataTermino,
        int vagas,
        UUID paroquiaId,
        String paroquiaNome,
        List<MembroDirigenteResponse> equipeDirigente,
        EquipeComandoResponse equipeComando,
        Instant createdAt) {
}
