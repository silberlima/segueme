package com.silberlima.segueme.posencontro.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Cobre a régua parametrizável de faixas de prioridade (RN-POS-03, pendência #14). */
class ReguaPrioridadeTest {

    private final ReguaPrioridade reguaDefault = new ReguaPrioridade(75, 40);

    /** Os exemplos citados em `pos-encontro.md` 3 (RN-POS-03): 12/12 = Alta, 3/12 = Baixa. */
    @ParameterizedTest
    @CsvSource({"100,ALTA", "75,ALTA", "74,MEDIA", "40,MEDIA", "39,BAIXA", "25,BAIXA", "0,BAIXA"})
    void shouldClassificarPercentualNaFaixaCorreta(int percentual, FaixaPrioridade esperada) {
        assertThat(reguaDefault.classificar(percentual)).isEqualTo(esperada);
    }

    @Test
    void shouldRespeitarReguaReparametrizada() {
        ReguaPrioridade maisExigente = new ReguaPrioridade(90, 60);

        assertThat(maisExigente.classificar(80)).isEqualTo(FaixaPrioridade.MEDIA);
        assertThat(reguaDefault.classificar(80)).isEqualTo(FaixaPrioridade.ALTA);
    }

    @Test
    void shouldRejeitarCorteDeAltaMenorOuIgualAoDeMedia() {
        assertThatThrownBy(() -> new ReguaPrioridade(40, 40)).isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> new ReguaPrioridade(30, 60)).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldRejeitarPercentualForaDaEscala() {
        assertThatThrownBy(() -> new ReguaPrioridade(120, 40)).isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> new ReguaPrioridade(75, -1)).isInstanceOf(BusinessRuleViolationException.class);
    }
}
