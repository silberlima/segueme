package com.silberlima.segueme.support;

import com.silberlima.segueme.pessoas.domain.Endereco;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dados obrigatórios de {@code Pessoa} que os testes precisam informar mas sobre os quais nada
 * afirmam. Existe para que cada teste continue dizendo só o que lhe interessa.
 *
 * <p>{@link #cpf()} devolve um CPF diferente a cada chamada, com dígitos verificadores válidos: o
 * campo é único no cadastro (RN-PESSOA-01), então uma constante quebraria qualquer teste que
 * persista duas pessoas — inclusive os que criam pessoas dentro de um método auxiliar chamado
 * várias vezes. A faixa começa em 7xx para não colidir com os CPFs do seed de demonstração.
 */
public final class PessoasDeTeste {

    private static final AtomicInteger PROXIMA_BASE = new AtomicInteger(700_000_000);

    private PessoasDeTeste() {
    }

    public static String cpf() {
        String base = String.valueOf(PROXIMA_BASE.getAndIncrement());
        String primeiro = digitoVerificador(base);
        return base + primeiro + digitoVerificador(base + primeiro);
    }

    public static Endereco endereco() {
        return new Endereco("01310-100", "Avenida Paulista", "1000", null, "Bela Vista", "São Paulo", "SP");
    }

    private static String digitoVerificador(String base) {
        int soma = 0;
        for (int i = 0; i < base.length(); i++) {
            soma += Character.getNumericValue(base.charAt(i)) * (base.length() + 1 - i);
        }
        int resto = 11 - (soma % 11);
        return String.valueOf(resto >= 10 ? 0 : resto);
    }
}
