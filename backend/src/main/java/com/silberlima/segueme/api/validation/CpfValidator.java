package com.silberlima.segueme.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Dígito verificador do CPF (módulo 11). Rejeita também as sequências de dígito repetido
 * ({@code 111.111.111-11} e afins): passam no cálculo do DV, mas não são CPFs emitidos, e é
 * exatamente o que alguém digita para escapar de um campo obrigatório.
 */
public class CpfValidator implements ConstraintValidator<Cpf, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        if (valor == null) {
            return true;
        }

        String digitos = valor.replaceAll("\\D", "");
        if (digitos.length() != 11 || digitos.chars().distinct().count() == 1) {
            return false;
        }

        String base = digitos.substring(0, 9);
        String primeiro = digitoVerificador(base);
        String segundo = digitoVerificador(base + primeiro);
        return digitos.equals(base + primeiro + segundo);
    }

    private String digitoVerificador(String base) {
        int soma = 0;
        for (int i = 0; i < base.length(); i++) {
            soma += Character.getNumericValue(base.charAt(i)) * (base.length() + 1 - i);
        }
        int resto = 11 - (soma % 11);
        return String.valueOf(resto >= 10 ? 0 : resto);
    }
}
