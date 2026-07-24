package com.silberlima.segueme.encontros.application.dto;

import com.silberlima.segueme.encontros.domain.PastaDirigente;
import com.silberlima.segueme.encontros.domain.TipoMembroDirigente;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Dados de entrada de {@code CriarEncontroUseCase} (RF-015 a RF-020), desacoplados do request da API. */
public record DadosNovoEncontro(
        String padroeiro,
        UUID diretorEspiritualId,
        String local,
        LocalDate dataInicio,
        LocalDate dataTermino,
        int vagas,
        UUID paroquiaId,
        List<MembroDirigente> equipeDirigente,
        UUID coordenadorGeralId,
        UUID casalCoordenadorId) {

    public DadosNovoEncontro {
        equipeDirigente = equipeDirigente == null ? List.of() : List.copyOf(equipeDirigente);
    }

    public record MembroDirigente(PastaDirigente pasta, TipoMembroDirigente tipoMembro, UUID pessoaId, UUID casalId) {
    }
}
