package com.silberlima.segueme.posencontro.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class FrequenciaPosEncontroTest {

    private final Paroquia paroquia = new Paroquia("Paróquia Teste", new Setor("Setor", new Diocese("Diocese")));
    private final EncontroPos encontroPos = new EncontroPos(paroquia, LocalDate.now(), null);
    private final Pessoa pessoa = new Pessoa(
            "Jovem Acompanhado",
            LocalDate.of(2005, 1, 1),
            "(11) 90000-0000",
            EstadoCivil.SOLTEIRO,
            paroquia,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    @Test
    void shouldExigirEncontroPosEPessoa() {
        assertThatThrownBy(() -> new FrequenciaPosEncontro(null, pessoa, true))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> new FrequenciaPosEncontro(encontroPos, null, true))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldRemarcarPresencaNoMesmoRegistro() {
        FrequenciaPosEncontro frequencia = new FrequenciaPosEncontro(encontroPos, pessoa, true);

        frequencia.registrar(false);

        assertThat(frequencia.isPresente()).isFalse();
    }
}
