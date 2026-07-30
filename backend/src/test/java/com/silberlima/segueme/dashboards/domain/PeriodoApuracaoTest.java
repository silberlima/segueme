package com.silberlima.segueme.dashboards.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Cobre a régua de apuração dos KPIs dos painéis: ano civil, mês civil e janela do gráfico mensal
 * (`dashboards-por-perfil.md` seção 5 — período exato pendente de validação com o PO).
 */
class PeriodoApuracaoTest {

    @Test
    void shouldApurarOAnoCivilCorrente() {
        PeriodoApuracao ano = PeriodoApuracao.anoDe(LocalDate.of(2026, 7, 29));

        assertThat(ano.de()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(ano.ate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void shouldApurarOMesCivilCorrente() {
        PeriodoApuracao mes = PeriodoApuracao.mesDe(LocalDate.of(2026, 2, 10));

        assertThat(mes.de()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(mes.ate()).isEqualTo(LocalDate.of(2026, 2, 28));
    }

    @Test
    void shouldIncluirOMesCorrenteNaJanelaRetroativa() {
        PeriodoApuracao janela = PeriodoApuracao.ultimosMeses(LocalDate.of(2026, 7, 29), 6);

        assertThat(janela.de()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(janela.ate()).isEqualTo(LocalDate.of(2026, 7, 31));
    }

    @Test
    void shouldThrowExceptionWhenJanelaMensalNaoTemMeses() {
        LocalDate hoje = LocalDate.of(2026, 7, 29);

        assertThatThrownBy(() -> PeriodoApuracao.ultimosMeses(hoje, 0))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowExceptionWhenDataInicialPosteriorAFinal() {
        LocalDate inicio = LocalDate.of(2026, 7, 29);
        LocalDate fim = LocalDate.of(2026, 7, 1);

        assertThatThrownBy(() -> new PeriodoApuracao(inicio, fim))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowExceptionWhenDataAusente() {
        assertThatThrownBy(() -> new PeriodoApuracao(null, LocalDate.of(2026, 7, 29)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
