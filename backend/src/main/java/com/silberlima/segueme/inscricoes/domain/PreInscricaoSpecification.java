package com.silberlima.segueme.inscricoes.domain;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import jakarta.persistence.criteria.JoinType;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Predicados combináveis da listagem de Pré-inscrições (`inscricoes.md` 2.1): encontro, status,
 * nome do candidato e escopo territorial (RN-04), aplicado via a paróquia do Encontro — mesmo
 * racional de {@code EncontroSpecification}/{@code PessoaSpecification}.
 */
public final class PreInscricaoSpecification {

    private PreInscricaoSpecification() {
    }

    public static Specification<PreInscricao> naoDeletada() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<PreInscricao> comEncontro(UUID encontroId) {
        if (encontroId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("encontro").get("id"), encontroId);
    }

    public static Specification<PreInscricao> comStatus(StatusPreInscricao status) {
        if (status == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<PreInscricao> comNomeCandidato(String nome) {
        if (nome == null || nome.isBlank()) {
            return Specification.unrestricted();
        }
        String termo = "%" + nome.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("pessoa").get("nomeCompleto")), termo);
    }

    public static Specification<PreInscricao> comEscopo(EscopoTerritorial escopo) {
        return switch (escopo) {
            case EscopoTerritorial.Nacional ignored -> Specification.unrestricted();
            case EscopoTerritorial.DeDiocese deDiocese -> (root, query, cb) -> cb.equal(
                    root.get("encontro").get("paroquia").get("setor").get("diocese").get("id"),
                    deDiocese.dioceseId());
            case EscopoTerritorial.DeSetor deSetor -> (root, query, cb) -> cb.equal(
                    root.get("encontro").get("paroquia").get("setor").get("id"), deSetor.setorId());
            case EscopoTerritorial.DeParoquia deParoquia -> (root, query, cb) -> cb.equal(
                    root.get("encontro").get("paroquia").get("id"), deParoquia.paroquiaId());
        };
    }

    /** Fetch join de {@code pessoa} e {@code pessoa.paroquia}, seguro com paginação (associações singulares). */
    public static Specification<PreInscricao> comPessoaCarregada() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("pessoa", JoinType.LEFT).fetch("paroquia", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }
}
