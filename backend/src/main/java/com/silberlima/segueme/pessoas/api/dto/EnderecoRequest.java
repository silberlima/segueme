package com.silberlima.segueme.pessoas.api.dto;

import com.silberlima.segueme.pessoas.domain.Endereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Endereço residencial, obrigatório no cadastro. Só {@code complemento} é opcional — apartamento,
 * bloco e ponto de referência não existem em todo endereço.
 */
public record EnderecoRequest(
        @NotBlank @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP inválido") String cep,
        @NotBlank String logradouro,
        @NotBlank String numero,
        String complemento,
        @NotBlank String bairro,
        @NotBlank String cidade,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$", message = "UF inválida") String uf) {

    public Endereco toDomain() {
        return new Endereco(cep, logradouro, numero, complemento, bairro, cidade, uf);
    }
}
