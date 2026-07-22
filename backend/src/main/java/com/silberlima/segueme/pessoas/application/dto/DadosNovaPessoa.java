package com.silberlima.segueme.pessoas.application.dto;

import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/** Dados de entrada de {@code CriarPessoaUseCase} (RF-009), desacoplados do DTO de request da API. */
public record DadosNovaPessoa(
        String nomeCompleto,
        LocalDate dataNascimento,
        String telefone,
        EstadoCivil estadoCivil,
        UUID paroquiaId,
        String alergias,
        String restricaoAlimentar,
        String medicacaoUsoContinuo,
        String contatoEmergenciaNome,
        String contatoEmergenciaTelefone,
        String contatoEmergenciaParentesco,
        UUID indicadoPorId,
        Set<UUID> competenciaIds) {

    public DadosNovaPessoa {
        competenciaIds = competenciaIds == null ? Set.of() : Set.copyOf(competenciaIds);
    }
}
