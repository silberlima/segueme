package com.silberlima.segueme.pessoas.api.dto;

import com.silberlima.segueme.api.validation.Cpf;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Campos obrigatórios conforme `cadastro-de-pessoas.md` 2.2 (nome/CPF/data de
 * nascimento/telefone/paróquia/endereço). Regex de telefone e obrigatoriedade exata são inferidas
 * do padrão da Ficha de Inscrição — não há especificação formal (pendência #17 de
 * `pendencias-gerais.md`).
 */
public record CriarPessoaRequest(
        @NotBlank String nomeCompleto,
        @NotBlank @Cpf String cpf,
        @NotNull @Past LocalDate dataNascimento,
        @NotBlank @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$", message = "Telefone inválido")
                String telefone,
        @Pattern(regexp = "^@?[A-Za-z0-9._]{1,30}$", message = "Perfil do Instagram inválido") String instagram,
        @NotNull EstadoCivil estadoCivil,
        @NotNull UUID paroquiaId,
        @NotNull @Valid EnderecoRequest endereco,
        String alergias,
        String restricaoAlimentar,
        String medicacaoUsoContinuo,
        String contatoEmergenciaNome,
        String contatoEmergenciaTelefone,
        String contatoEmergenciaParentesco,
        UUID indicadoPorId,
        Set<UUID> competenciaIds) {

    public CriarPessoaRequest {
        competenciaIds = competenciaIds == null ? Set.of() : Set.copyOf(competenciaIds);
    }
}
