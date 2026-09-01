package com.silberlima.segueme.pessoas.api.dto;

import com.silberlima.segueme.api.validation.Cpf;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * O que a própria pessoa altera sobre si (perfil Seguidor). CPF, data de nascimento, telefone e
 * endereço seguem obrigatórios e com o mesmo formato do cadastro — a edição corrige esses dados,
 * nunca os apaga. Instagram, saúde e contato de emergência são opcionais: nem todo mundo tem
 * perfil, alergia, restrição ou contato de emergência a declarar.
 */
public record AtualizarMeusDadosRequest(
        @NotBlank @Cpf String cpf,
        @NotNull @Past LocalDate dataNascimento,
        @NotBlank @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$", message = "Telefone inválido")
                String telefone,
        @Pattern(regexp = "^@?[A-Za-z0-9._]{1,30}$", message = "Perfil do Instagram inválido") String instagram,
        @NotNull @Valid EnderecoRequest endereco,
        String alergias,
        String restricaoAlimentar,
        String medicacaoUsoContinuo,
        String contatoEmergenciaNome,
        String contatoEmergenciaTelefone,
        String contatoEmergenciaParentesco) {
}
