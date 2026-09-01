package com.silberlima.segueme.financeiro.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Cobre as transições de estado de {@link Campanha} (RF-050, RN-FINANCEIRO-03). */
class CampanhaTest {

    private final Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
    private final Pessoa diretor = new Pessoa(
            "Diretor",
            PessoasDeTeste.cpf(),
            LocalDate.of(1980, 1, 1),
            "(11) 90000-0000",
            null,
            EstadoCivil.SOLTEIRO,
            paroquia,
            PessoasDeTeste.endereco(),
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    private final Encontro encontro =
            new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);

    @Test
    void shouldStartAtivaWithZeroArrecadado() {
        Campanha campanha = new Campanha(encontro, "Festa Junina", new BigDecimal("1000.00"));

        assertThat(campanha.getStatus()).isEqualTo(StatusCampanha.ATIVA);
        assertThat(campanha.getValorArrecadado()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldRejectNonPositiveMeta() {
        assertThatThrownBy(() -> new Campanha(encontro, "Festa Junina", BigDecimal.ZERO))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldAccumulateArrecadacao() {
        Campanha campanha = new Campanha(encontro, "Festa Junina", new BigDecimal("1000.00"));

        campanha.registrarArrecadacao(new BigDecimal("300.00"));
        campanha.registrarArrecadacao(new BigDecimal("200.00"));

        assertThat(campanha.getValorArrecadado()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void shouldRejectNonPositiveArrecadacao() {
        Campanha campanha = new Campanha(encontro, "Festa Junina", new BigDecimal("1000.00"));

        assertThatThrownBy(() -> campanha.registrarArrecadacao(BigDecimal.ZERO))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldEncerrarAtivaCampanha() {
        Campanha campanha = new Campanha(encontro, "Festa Junina", new BigDecimal("1000.00"));

        campanha.encerrar();

        assertThat(campanha.getStatus()).isEqualTo(StatusCampanha.ENCERRADA);
    }

    @Test
    void shouldNotEncerrarTwice() {
        Campanha campanha = new Campanha(encontro, "Festa Junina", new BigDecimal("1000.00"));
        campanha.encerrar();

        assertThatThrownBy(campanha::encerrar).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldNotRegistrarArrecadacaoAfterEncerrar() {
        Campanha campanha = new Campanha(encontro, "Festa Junina", new BigDecimal("1000.00"));
        campanha.encerrar();

        assertThatThrownBy(() -> campanha.registrarArrecadacao(new BigDecimal("10.00")))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
