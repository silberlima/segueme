package com.silberlima.segueme.conteudo.domain;

import org.springframework.data.jpa.domain.Specification;

/** Predicados combináveis de {@link Palestrante}: exclusão lógica e tipo. */
public final class PalestranteSpecification {

    private PalestranteSpecification() {
    }

    public static Specification<Palestrante> naoDeletado() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Palestrante> comTipo(TipoPalestrante tipo) {
        if (tipo == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }
}
