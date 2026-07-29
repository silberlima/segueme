package com.silberlima.segueme.posencontro.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.silberlima.segueme.posencontro.domain.FaixaPrioridade;
import com.silberlima.segueme.posencontro.domain.ReguaPrioridade;
import org.junit.jupiter.api.Test;

/** Cobre a apuração do percentual e da faixa (RN-POS-03) com os exemplos do próprio doc. */
class FrequenciaPessoaTest {

    private final ReguaPrioridade regua = new ReguaPrioridade(75, 40);

    @Test
    void shouldApurarPresencaTotalComoAlta() {
        FrequenciaPessoa frequencia = FrequenciaPessoa.apurar(null, 12, 12, regua);

        assertThat(frequencia.percentual()).isEqualTo(100);
        assertThat(frequencia.faixa()).isEqualTo(FaixaPrioridade.ALTA);
    }

    @Test
    void shouldApurarTresDeDozeComoBaixa() {
        FrequenciaPessoa frequencia = FrequenciaPessoa.apurar(null, 12, 3, regua);

        assertThat(frequencia.percentual()).isEqualTo(25);
        assertThat(frequencia.faixa()).isEqualTo(FaixaPrioridade.BAIXA);
    }

    @Test
    void shouldApurarZeroQuandoParoquiaNaoRealizouEncontroPos() {
        FrequenciaPessoa frequencia = FrequenciaPessoa.apurar(null, 0, 0, regua);

        assertThat(frequencia.percentual()).isZero();
        assertThat(frequencia.faixa()).isEqualTo(FaixaPrioridade.BAIXA);
    }

    @Test
    void shouldArredondarPercentual() {
        assertThat(FrequenciaPessoa.apurar(null, 3, 2, regua).percentual()).isEqualTo(67);
    }
}
