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

    /**
     * Se o registro daquele território é visível neste escopo (RNF-06). É a versão em memória do
     * mesmo critério que as {@code Specification} aplicam em consulta — usada quando o dado já foi
     * localizado por um id vindo da requisição e o que falta é decidir se o usuário pode vê-lo.
     */
    default boolean contem(Territorio territorio) {
        return switch (this) {
            case Nacional ignored -> true;
            case DeDiocese deDiocese -> deDiocese.dioceseId().equals(territorio.dioceseId());
            case DeSetor deSetor -> deSetor.setorId().equals(territorio.setorId());
            case DeParoquia deParoquia -> deParoquia.paroquiaId().equals(territorio.paroquiaId());
        };
    }
}
