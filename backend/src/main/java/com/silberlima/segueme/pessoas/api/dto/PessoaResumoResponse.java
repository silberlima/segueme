package com.silberlima.segueme.pessoas.api.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Linha da Lista de Pessoas (RF-007). {@code numEncontros} é placeholder até a Fase 4 (Encontro) existir. */
public record PessoaResumoResponse(
        UUID id,
        String nomeCompleto,
        LocalDate dataNascimento,
        String estadoCivil,
        String situacao,
        UUID paroquiaId,
        String paroquiaNome,
        List<String> competencias,
        int numEncontros) {
}
