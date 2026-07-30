package com.silberlima.segueme.relatorios.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Converte valores do domínio no texto que vai para a célula do arquivo. Fica num lugar só para
 * que os seis geradores escrevam data, valor e campo ausente da mesma forma — um relatório com
 * três formatos de data seria um relatório errado aos olhos de quem abre a planilha.
 */
public final class CelulaDeRelatorio {

    private static final String VAZIO = "";
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private CelulaDeRelatorio() {
    }

    public static String de(String texto) {
        return texto == null ? VAZIO : texto;
    }

    public static String de(LocalDate data) {
        return data == null ? VAZIO : DATA.format(data);
    }

    public static String de(Instant instante) {
        return instante == null ? VAZIO : DATA_HORA.format(instante);
    }

    /** Decimal com vírgula, como a planilha em português espera. */
    public static String de(BigDecimal valor) {
        return valor == null ? VAZIO : valor.toPlainString().replace('.', ',');
    }

    public static String de(long numero) {
        return String.valueOf(numero);
    }

    public static String de(Enum<?> valor) {
        return valor == null ? VAZIO : valor.name();
    }
}
