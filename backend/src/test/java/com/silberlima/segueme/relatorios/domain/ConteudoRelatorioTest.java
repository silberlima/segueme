package com.silberlima.segueme.relatorios.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Cobre a serialização CSV do conteúdo do relatório (pendência #37: formato de saída do MVP). */
class ConteudoRelatorioTest {

    @Test
    void shouldEscreverCabecalhoELinhasSeparadosPorPontoEVirgula() {
        ConteudoRelatorio conteudo = new ConteudoRelatorio(
                List.of("Nome", "Situação"), List.of(List.of("Ana", "SEGUIMISTA"), List.of("Bruno", "SEGUIDOR")));

        assertThat(conteudo.paraCsv()).isEqualTo("Nome;Situação\nAna;SEGUIMISTA\nBruno;SEGUIDOR");
    }

    @Test
    void shouldEnvolverEmAspasACelulaQueContemOSeparador() {
        ConteudoRelatorio conteudo =
                new ConteudoRelatorio(List.of("Justificativa"), List.of(List.of("Viajo no dia; não consigo servir")));

        assertThat(conteudo.paraCsv()).contains("\"Viajo no dia; não consigo servir\"");
    }

    @Test
    void shouldDobrarAsAspasInternas() {
        ConteudoRelatorio conteudo =
                new ConteudoRelatorio(List.of("Observação"), List.of(List.of("Chamou de \"reserva\"")));

        assertThat(conteudo.paraCsv()).contains("\"Chamou de \"\"reserva\"\"\"");
    }

    @Test
    void shouldEscreverSoOCabecalhoQuandoNaoHaDado() {
        ConteudoRelatorio conteudo = ConteudoRelatorio.vazio(List.of("Nome", "Situação"));

        assertThat(conteudo.totalLinhas()).isZero();
        assertThat(conteudo.paraCsv()).isEqualTo("Nome;Situação");
    }
}
