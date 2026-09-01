package com.silberlima.segueme.pessoas.api.dto;

public record EnderecoResponse(
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf) {
}
