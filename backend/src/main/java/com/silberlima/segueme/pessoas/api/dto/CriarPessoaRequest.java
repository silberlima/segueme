package com.silberlima.segueme.pessoas.api.dto;

import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Campos obrigatórios conforme `cadastro-de-pessoas.md` 2.2 (nome/data de nascimento/telefone/paróquia).
 * Regex de telefone e obrigatoriedade exata são inferidas do padrão da Ficha de Inscrição — não
 * há especificação formal (pendência #17 de `pendencias-gerais.md`).
 */
public record CriarPessoaRequest(
        @NotBlank String nomeCompleto,
        @NotNull @Past LocalDate dataNascimento,
        @NotBlank @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$", message = "Telefone inválido")
                String telefone,
        @NotNull EstadoCivil estadoCivil,
        @NotNull UUID paroquiaId,
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
