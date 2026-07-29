package com.silberlima.segueme.conteudo.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Cobre {@link DesignacaoPalestra} (RF-052, UC — Designar palestrante). */
class DesignacaoPalestraTest {

    private final Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
    private final Pessoa diretor = new Pessoa(
            "Diretor", LocalDate.of(1980, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia,
            null, null, null, null, null, null, null);
    private final Encontro encontro =
            new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);
    private final Palestra palestra = new Palestra(1, "Tema 1");

    @Test
    void shouldStartWithoutPalestrante() {
        DesignacaoPalestra designacao = new DesignacaoPalestra(encontro, palestra);

        assertThat(designacao.getPalestrante()).isNull();
        assertThat(designacao.getEncontro()).isEqualTo(encontro);
        assertThat(designacao.getPalestra()).isEqualTo(palestra);
    }

    @Test
    void shouldAllowChangingPalestrante() {
        DesignacaoPalestra designacao = new DesignacaoPalestra(encontro, palestra);
        Palestrante primeiro = Palestrante.externo("Convidado 1");
        Palestrante segundo = Palestrante.externo("Convidado 2");

        designacao.designar(primeiro);
        assertThat(designacao.getPalestrante()).isEqualTo(primeiro);

        designacao.designar(segundo);
        assertThat(designacao.getPalestrante()).isEqualTo(segundo);
    }
}
