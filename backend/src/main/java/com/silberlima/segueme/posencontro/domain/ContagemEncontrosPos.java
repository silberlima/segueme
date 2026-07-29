package com.silberlima.segueme.posencontro.domain;

import java.util.UUID;

/** Quantos encontros pós uma paróquia realizou na janela apurada. */
public interface ContagemEncontrosPos {

    UUID getParoquiaId();

    Long getTotal();
}
