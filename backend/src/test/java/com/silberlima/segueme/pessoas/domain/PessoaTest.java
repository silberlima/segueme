package com.silberlima.segueme.pessoas.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Cobre {@link Pessoa#transferirParaParoquia(Paroquia)} (RF-041, RN-MOVIMENTO-01, Fase 8). */
class PessoaTest {

    @Test
    void shouldChangeParoquiaOnTransfer() {
        Paroquia origem = new Paroquia("Origem", new Setor("Setor", new Diocese("Diocese")));
        Paroquia destino = new Paroquia("Destino", new Setor("Setor", new Diocese("Diocese")));
        Pessoa pessoa = new Pessoa(
                "Candidato",
                PessoasDeTeste.cpf(),
                LocalDate.of(2000, 1, 1),
                "(11) 90000-0000",
                null,
                EstadoCivil.SOLTEIRO,
                origem,
                PessoasDeTeste.endereco(),
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        pessoa.transferirParaParoquia(destino);

        assertThat(pessoa.getParoquia()).isEqualTo(destino);
    }
}
