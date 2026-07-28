package com.silberlima.segueme.movimentacao.domain;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Predicados combináveis de {@link Emprestimo}: status, encontro/equipe e escopo territorial —
 * este último resolvido a partir da paróquia do {@code Encontro} solicitante (mesmo racional de
 * {@link com.silberlima.segueme.pessoas.domain.PessoaSpecification#comEscopo}).
 */
public final class EmprestimoSpecification {

    private EmprestimoSpecification() {
    }

    public static Specification<Emprestimo> naoDeletado() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Emprestimo> comStatus(StatusEmprestimo status) {
        if (status == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Emprestimo> comEncontro(UUID encontroId) {
        if (encontroId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("encontro").get("id"), encontroId);
    }

    public static Specification<Emprestimo> comEquipe(UUID equipeId) {
        if (equipeId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("equipe").get("id"), equipeId);
    }

    public static Specification<Emprestimo> comEscopo(EscopoTerritorial escopo) {
        return switch (escopo) {
            case EscopoTerritorial.Nacional ignored -> Specification.unrestricted();
            case EscopoTerritorial.DeDiocese deDiocese -> (root, query, cb) -> cb.equal(
                    root.get("encontro").get("paroquia").get("setor").get("diocese").get("id"), deDiocese.dioceseId());
            case EscopoTerritorial.DeSetor deSetor -> (root, query, cb) ->
                    cb.equal(root.get("encontro").get("paroquia").get("setor").get("id"), deSetor.setorId());
            case EscopoTerritorial.DeParoquia deParoquia -> (root, query, cb) ->
                    cb.equal(root.get("encontro").get("paroquia").get("id"), deParoquia.paroquiaId());
        };
    }
}
