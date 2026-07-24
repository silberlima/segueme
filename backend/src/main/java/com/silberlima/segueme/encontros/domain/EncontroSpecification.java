package com.silberlima.segueme.encontros.domain;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import jakarta.persistence.criteria.JoinType;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Predicados combináveis da Lista de Encontros (RF-014): número, status, paróquia e escopo
 * territorial (RN-04) — mesmo racional de {@code PessoaSpecification}.
 */
public final class EncontroSpecification {

    private EncontroSpecification() {
    }

    public static Specification<Encontro> naoDeletado() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Encontro> comNumero(Integer numero) {
        if (numero == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("numero"), numero);
    }

    public static Specification<Encontro> comStatus(StatusEncontro status) {
        if (status == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Encontro> comParoquia(UUID paroquiaId) {
        if (paroquiaId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("paroquia").get("id"), paroquiaId);
    }

    /** Fetch join de {@code paroquia}, seguro com paginação por ser associação singular. */
    public static Specification<Encontro> comParoquiaCarregada() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("paroquia", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    /**
     * Fetch join de {@code equipeComando.coordenadorGeral}, para exibir o coordenador geral na
     * Lista de Encontros sem N+1 (RF-014).
     */
    public static Specification<Encontro> comEquipeComandoCarregada() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("equipeComando", JoinType.LEFT).fetch("coordenadorGeral", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    public static Specification<Encontro> comEscopo(EscopoTerritorial escopo) {
        return switch (escopo) {
            case EscopoTerritorial.Nacional ignored -> Specification.unrestricted();
            case EscopoTerritorial.DeDiocese deDiocese -> (root, query, cb) -> cb.equal(
                    root.get("paroquia").get("setor").get("diocese").get("id"), deDiocese.dioceseId());
            case EscopoTerritorial.DeSetor deSetor -> (root, query, cb) ->
                    cb.equal(root.get("paroquia").get("setor").get("id"), deSetor.setorId());
            case EscopoTerritorial.DeParoquia deParoquia -> (root, query, cb) ->
                    cb.equal(root.get("paroquia").get("id"), deParoquia.paroquiaId());
        };
    }
}
