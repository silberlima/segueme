package com.silberlima.segueme.posencontro.domain;

import java.time.LocalDate;

/** Projeção Spring Data de quantas presenças um encontro pós teve, pela data em que ocorreu. */
public interface ContagemPresencasPorData {

    LocalDate getData();

    Long getTotal();
}
