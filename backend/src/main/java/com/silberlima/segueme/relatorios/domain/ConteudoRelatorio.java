package com.silberlima.segueme.relatorios.domain;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Resultado tabular de um {@link GeradorDeRelatorio}, independente do formato de saída.
 *
 * <p><strong>Pendência #37 de {@code docs/pendencias-gerais.md}:</strong> PDF ou planilha não foi
 * decidido. O MVP serializa em CSV, que qualquer planilha abre; o gerador devolve linhas e colunas,
 * então trocar o formato depois é escrever outro serializador, não refazer os seis geradores.
 */
public record ConteudoRelatorio(List<String> colunas, List<List<String>> linhas) {

    private static final char SEPARADOR = ';';

    public static ConteudoRelatorio vazio(List<String> colunas) {
        return new ConteudoRelatorio(colunas, List.of());
    }

    public int totalLinhas() {
        return linhas.size();
    }

    /**
     * CSV com separador {@code ;} (o que o Excel em português assume) e aspas conforme RFC 4180 —
     * necessário porque campos livres do domínio (justificativa de recusa, categoria de lançamento)
     * podem conter o próprio separador.
     */
    public String paraCsv() {
        return Stream.concat(Stream.of(colunas), linhas.stream())
                .map(ConteudoRelatorio::linhaCsv)
                .collect(Collectors.joining("\n"));
    }

    private static String linhaCsv(List<String> celulas) {
        return celulas.stream().map(ConteudoRelatorio::celulaCsv).collect(Collectors.joining(String.valueOf(SEPARADOR)));
    }

    private static String celulaCsv(String valor) {
        String texto = valor == null ? "" : valor;
        if (texto.indexOf(SEPARADOR) < 0 && texto.indexOf('"') < 0 && texto.indexOf('\n') < 0) {
            return texto;
        }
        return '"' + texto.replace("\"", "\"\"") + '"';
    }
}
