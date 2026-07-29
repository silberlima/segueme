package com.silberlima.segueme.posencontro.domain;

import java.util.UUID;

/**
 * Presenças confirmadas de uma pessoa na janela apurada. {@code paroquiaId} é a paróquia onde os
 * encontros pós aconteceram — e não a paróquia atual da pessoa, que pode ter mudado por
 * transferência (Fase 8) — porque é ela que dá o denominador correto: quantos encontros pós aquela
 * paróquia realizou no período.
 */
public interface ContagemPresencas {

    UUID getPessoaId();

    UUID getParoquiaId();

    Long getPresencas();
}
