package com.silberlima.segueme.conteudo.domain;

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

/** Cobre as regras dos 3 tipos de {@link Palestrante} (RF-052, RN-CONTEUDO-02). */
class PalestranteTest {

    private final Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
    private final Pessoa pessoa = new Pessoa(
            "Seguidor Palestrante", LocalDate.of(1990, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia,
            null, null, null, null, null, null, null);

    @Test
    void shouldCreateSeguidorLinkedToPessoa() {
        Palestrante palestrante = Palestrante.seguidor(pessoa);

        assertThat(palestrante.getTipo()).isEqualTo(TipoPalestrante.SEGUIDOR);
        assertThat(palestrante.getPessoa()).isEqualTo(pessoa);
        assertThat(palestrante.getNome()).isEqualTo(pessoa.getNomeCompleto());
    }

    @Test
    void shouldCreateExternoWithoutPessoa() {
        Palestrante palestrante = Palestrante.externo("Convidado Externo");

        assertThat(palestrante.getTipo()).isEqualTo(TipoPalestrante.EXTERNO);
        assertThat(palestrante.getPessoa()).isNull();
    }

    @Test
    void shouldCreateCleroWithoutPessoa() {
        Palestrante palestrante = Palestrante.clero("Padre João");

        assertThat(palestrante.getTipo()).isEqualTo(TipoPalestrante.CLERO);
        assertThat(palestrante.getPessoa()).isNull();
    }

    @Test
    void shouldRejectBlankNomeForExterno() {
        assertThatThrownBy(() -> Palestrante.externo(" ")).isInstanceOf(BusinessRuleViolationException.class);
    }
}
