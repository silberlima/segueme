package com.silberlima.segueme.movimentacao.domain;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import jakarta.persistence.criteria.JoinType;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Predicados combináveis de {@link Transferencia}: status e escopo territorial do Setorial
 * decisor. Uma Transferência envolve duas paróquias (origem e destino), possivelmente em setores
 * diferentes — {@link #comEscopo(EscopoTerritorial)} usa OR entre as duas para que o Setorial de
 * qualquer um dos lados enxergue e decida a solicitação.
 */
public final class TransferenciaSpecification {

    private TransferenciaSpecification() {
    }

    public static Specification<Transferencia> naoDeletada() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Transferencia> comStatus(StatusTransferencia status) {
        if (status == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /**
     * Solicitada a partir de um instante — recorte do KPI "Transferências no mês" do Painel do
     * Setor (`dashboards-por-perfil.md` 3.3). Conta pela data de solicitação, não de aprovação:
     * o período exato de apuração dos KPIs é pendência de validação com o PO (seção 5 do mesmo doc).
     */
    public static Specification<Transferencia> solicitadaDesde(Instant desde) {
        if (desde == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), desde);
    }

    public static Specification<Transferencia> comPessoa(UUID pessoaId) {
        if (pessoaId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("pessoa").get("id"), pessoaId);
    }

    public static Specification<Transferencia> comEscopo(EscopoTerritorial escopo) {
        return switch (escopo) {
            case EscopoTerritorial.Nacional ignored -> Specification.unrestricted();
            case EscopoTerritorial.DeDiocese deDiocese -> (root, query, cb) -> cb.or(
                    cb.equal(root.get("paroquiaOrigem").get("setor").get("diocese").get("id"), deDiocese.dioceseId()),
                    cb.equal(root.get("paroquiaDestino").get("setor").get("diocese").get("id"), deDiocese.dioceseId()));
            case EscopoTerritorial.DeSetor deSetor -> (root, query, cb) -> cb.or(
                    cb.equal(root.get("paroquiaOrigem").get("setor").get("id"), deSetor.setorId()),
                    cb.equal(root.get("paroquiaDestino").get("setor").get("id"), deSetor.setorId()));
            case EscopoTerritorial.DeParoquia deParoquia -> (root, query, cb) -> cb.or(
                    cb.equal(root.get("paroquiaOrigem").get("id"), deParoquia.paroquiaId()),
                    cb.equal(root.get("paroquiaDestino").get("id"), deParoquia.paroquiaId()));
        };
    }
    /**
     * Fetch join das associações que a resposta lê. Necessário porque {@code open-in-view} está
     * desligado e o mapeamento para DTO ocorre no Controller, fora da transação do Use Case —
     * sem isto a listagem estoura {@code LazyInitializationException}. Ignorado na sub-consulta de
     * contagem do Spring Data (paginação).
     */
    public static Specification<Transferencia> comAssociacoesCarregadas() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("pessoa", JoinType.LEFT);
                root.fetch("paroquiaOrigem", JoinType.LEFT);
                root.fetch("paroquiaDestino", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }
}
