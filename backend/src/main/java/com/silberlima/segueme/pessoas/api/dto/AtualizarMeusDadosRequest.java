package com.silberlima.segueme.pessoas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * O que a própria pessoa altera sobre si (perfil Seguidor). Telefone segue obrigatório e com o
 * mesmo formato do cadastro — é por ele que a equipe encontra a pessoa; o resto é opcional porque
 * nem todo mundo tem alergia, restrição ou contato de emergência a declarar.
 */
public record AtualizarMeusDadosRequest(
        @NotBlank @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$", message = "Telefone inválido")
                String telefone,
        String alergias,
        String restricaoAlimentar,
        String medicacaoUsoContinuo,
        String contatoEmergenciaNome,
        String contatoEmergenciaTelefone,
        String contatoEmergenciaParentesco) {
}
