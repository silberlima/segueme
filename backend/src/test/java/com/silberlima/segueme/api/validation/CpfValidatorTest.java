package com.silberlima.segueme.api.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CpfValidatorTest {

    private final CpfValidator validator = new CpfValidator();

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-25", "52998224725", "111.444.777-35", "390 533 447 05"})
    void shouldAcceptCpfWithValidCheckDigits(String cpf) {
        assertThat(validator.isValid(cpf, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-00", "12345678900", "5299822472", "529982247256", "abc"})
    void shouldRejectCpfWithWrongCheckDigitsOrLength(String cpf) {
        assertThat(validator.isValid(cpf, null)).isFalse();
    }

    /** Passam no cálculo do DV, mas não são CPFs emitidos — e são o que se digita para escapar. */
    @ParameterizedTest
    @ValueSource(strings = {"000.000.000-00", "111.111.111-11", "99999999999"})
    void shouldRejectRepeatedDigits(String cpf) {
        assertThat(validator.isValid(cpf, null)).isFalse();
    }

    /** Obrigatoriedade é de {@code @NotBlank}: nulo passa para não duplicar a mensagem de erro. */
    @Test
    void shouldLeaveNullToNotBlank() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
