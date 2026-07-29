package com.silberlima.segueme.financeiro.domain;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Predicados combináveis de {@link Campanha}: encontro, status e escopo territorial. */
public final class CampanhaSpecification {

    private CampanhaSpecification() {
    }

    public static Specification<Campanha> naoDeletada() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Campanha> comEncontro(UUID encontroId) {
        if (encontroId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("encontro").get("id"), encontroId);
    }

    public static Specification<Campanha> comStatus(StatusCampanha status) {
        if (status == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Campanha> comEscopo(EscopoTerritorial escopo) {
        return switch (escopo) {
            case EscopoTerritorial.Nacional ignored -> Specification.unrestricted();
            case EscopoTerritorial.DeDiocese deDiocese -> (root, query, cb) -> cb.equal(
                    root.get("encontro").get("paroquia").get("setor").get("diocese").get("id"), deDiocese.dioceseId());
            case EscopoTerritorial.DeSetor deSetor -> (root, query, cb) -> cb.equal(
                    root.get("encontro").get("paroquia").get("setor").get("id"), deSetor.setorId());
            case EscopoTerritorial.DeParoquia deParoquia -> (root, query, cb) -> cb.equal(
                    root.get("encontro").get("paroquia").get("id"), deParoquia.paroquiaId());
        };
    }
}
