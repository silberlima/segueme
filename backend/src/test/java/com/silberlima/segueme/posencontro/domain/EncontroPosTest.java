package com.silberlima.segueme.posencontro.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EncontroPosTest {

    private final Paroquia paroquia = new Paroquia("Paróquia Teste", new Setor("Setor", new Diocese("Diocese")));

    @Test
    void shouldExigirParoquiaEData() {
        assertThatThrownBy(() -> new EncontroPos(null, LocalDate.now(), null))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> new EncontroPos(paroquia, null, null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldNormalizarTemaVazioParaNulo() {
        assertThat(new EncontroPos(paroquia, LocalDate.now(), "   ").getTema()).isNull();
        assertThat(new EncontroPos(paroquia, LocalDate.now(), " Perseverança ").getTema()).isEqualTo("Perseverança");
    }
}
