package com.silberlima.segueme.pessoas.application.dto;

import com.silberlima.segueme.pessoas.domain.Endereco;
import java.time.LocalDate;

/**
 * O que a própria pessoa pode alterar sobre si (perfil Seguidor). O recorte — e o porquê de nome,
 * paróquia, situação e competências ficarem de fora — está em
 * {@code Pessoa#atualizarDadosPessoais}.
 */
public record DadosPessoaisEditaveis(
        String cpf,
        LocalDate dataNascimento,
        String telefone,
        String instagram,
        Endereco endereco,
        String alergias,
        String restricaoAlimentar,
        String medicacaoUsoContinuo,
        String contatoEmergenciaNome,
        String contatoEmergenciaTelefone,
        String contatoEmergenciaParentesco) {
}
