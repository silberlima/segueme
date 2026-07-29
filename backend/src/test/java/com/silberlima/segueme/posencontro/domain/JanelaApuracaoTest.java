package com.silberlima.segueme.posencontro.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class JanelaApuracaoTest {

    @Test
    void shouldApurarAteHojeQuandoPeriodoNaoInformado() {
        JanelaApuracao janela = JanelaApuracao.normalizar(null, null);

        assertThat(janela.ate()).isEqualTo(LocalDate.now());
        assertThat(janela.de()).isBefore(janela.ate());
    }

    @Test
    void shouldPreservarPeriodoInformado() {
        LocalDate de = LocalDate.of(2026, 1, 1);
        LocalDate ate = LocalDate.of(2026, 3, 31);

        assertThat(JanelaApuracao.normalizar(de, ate)).isEqualTo(new JanelaApuracao(de, ate));
    }

    @Test
    void shouldRejeitarDataInicialPosteriorAFinal() {
        assertThatThrownBy(() -> JanelaApuracao.normalizar(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 1)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
