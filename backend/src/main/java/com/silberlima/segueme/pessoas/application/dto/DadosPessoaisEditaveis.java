package com.silberlima.segueme.pessoas.application.dto;

/**
 * O que a própria pessoa pode alterar sobre si (perfil Seguidor). O recorte — e o porquê de nome,
 * data de nascimento, paróquia, situação e competências ficarem de fora — está em
 * {@code Pessoa#atualizarDadosPessoais}.
 */
public record DadosPessoaisEditaveis(
        String telefone,
        String alergias,
        String restricaoAlimentar,
        String medicacaoUsoContinuo,
        String contatoEmergenciaNome,
        String contatoEmergenciaTelefone,
        String contatoEmergenciaParentesco) {
}
