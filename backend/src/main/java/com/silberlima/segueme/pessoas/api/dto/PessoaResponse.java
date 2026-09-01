package com.silberlima.segueme.pessoas.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PessoaResponse(
        UUID id,
        String nomeCompleto,
        String cpf,
        LocalDate dataNascimento,
        String telefone,
        String instagram,
        String estadoCivil,
        String situacao,
        UUID paroquiaId,
        String paroquiaNome,
        EnderecoResponse endereco,
        String alergias,
        String restricaoAlimentar,
        String medicacaoUsoContinuo,
        String contatoEmergenciaNome,
        String contatoEmergenciaTelefone,
        String contatoEmergenciaParentesco,
        List<String> competencias,
        UUID indicadoPorId,
        String indicadoPorNome,
        Instant createdAt) {
}
