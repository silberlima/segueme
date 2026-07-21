package com.silberlima.segueme.hierarquia.domain;

import java.util.UUID;

/**
 * Escopo territorial de um usuário autenticado (RN-04): determina até onde ele enxerga a
 * hierarquia Nacional → Diocese → Setor → Paróquia. Resolvido por {@link EscopoTerritorialResolver}
 * e consumido por qualquer domínio futuro que precise filtrar dados por território.
 */
public sealed interface EscopoTerritorial {

    record Nacional() implements EscopoTerritorial {
    }

    record DeDiocese(UUID dioceseId) implements EscopoTerritorial {
    }

    record DeSetor(UUID setorId) implements EscopoTerritorial {
    }

    record DeParoquia(UUID paroquiaId) implements EscopoTerritorial {
    }
}
