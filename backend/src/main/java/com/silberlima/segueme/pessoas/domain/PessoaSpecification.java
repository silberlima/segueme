package com.silberlima.segueme.pessoas.domain;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Predicados combináveis da busca de Pessoa (RF-007): nome, paróquia, competência e escopo
 * territorial (RN-04). Um {@link Specification} é usado aqui — e não métodos derivados como em
 * {@code ParoquiaRepository} — porque os filtros de busca são todos opcionais e combináveis
 * entre si, o que explodiria em métodos derivados (4 variações de escopo × filtros opcionais).
 */
public final class PessoaSpecification {

    private PessoaSpecification() {
    }

    public static Specification<Pessoa> naoDeletada() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Pessoa> comNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return Specification.unrestricted();
        }
        String termo = "%" + nome.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("nomeCompleto")), termo);
    }

    public static Specification<Pessoa> comParoquia(UUID paroquiaId) {
        if (paroquiaId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("paroquia").get("id"), paroquiaId);
    }

    public static Specification<Pessoa> comSituacao(SituacaoPessoa situacao) {
        if (situacao == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("situacao"), situacao);
    }

    public static Specification<Pessoa> comCompetencia(UUID competenciaId) {
        if (competenciaId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> {
            Join<Pessoa, Competencia> competencias = root.join("competencias");
            return cb.equal(competencias.get("id"), competenciaId);
        };
    }

    /**
     * Faz fetch join de {@code paroquia} (associação singular, segura com paginação — ao
     * contrário de {@code competencias}, uma coleção). Necessário porque {@code open-in-view}
     * está desligado (`application.yml`) e o mapeamento para DTO ocorre no Controller, fora da
     * transação do Use Case. Ignorado na sub-consulta de contagem do Spring Data (paginação).
     */
    public static Specification<Pessoa> comParoquiaCarregada() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("paroquia", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    public static Specification<Pessoa> comEscopo(EscopoTerritorial escopo) {
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
