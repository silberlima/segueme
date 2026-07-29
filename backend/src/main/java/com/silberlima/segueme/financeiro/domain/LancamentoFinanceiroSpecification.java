package com.silberlima.segueme.financeiro.domain;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Predicados combináveis de {@link LancamentoFinanceiro}: encontro, tipo, situação e escopo
 * territorial via {@code encontro.paroquia} (associação singular — mesmo racional de
 * {@code EncontroSpecification.comEscopo}).
 */
public final class LancamentoFinanceiroSpecification {

    private LancamentoFinanceiroSpecification() {
    }

    public static Specification<LancamentoFinanceiro> naoDeletado() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<LancamentoFinanceiro> comEncontro(UUID encontroId) {
        if (encontroId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("encontro").get("id"), encontroId);
    }

    public static Specification<LancamentoFinanceiro> comTipo(TipoLancamento tipo) {
        if (tipo == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<LancamentoFinanceiro> comSituacao(SituacaoDespesa situacao) {
        if (situacao == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("situacao"), situacao);
    }

    public static Specification<LancamentoFinanceiro> comEscopo(EscopoTerritorial escopo) {
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
