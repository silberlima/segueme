package com.silberlima.segueme.financeiro.domain;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LancamentoFinanceiroRepository
        extends JpaRepository<LancamentoFinanceiro, UUID>, JpaSpecificationExecutor<LancamentoFinanceiro> {

    @EntityGraph(attributePaths = {"encontro"})
    Optional<LancamentoFinanceiro> findByIdAndDeletedFalse(UUID id);

    /** Soma dos lançamentos de um tipo no Encontro, base do saldo calculado (RN-FINANCEIRO-04). */
    @Query("select coalesce(sum(l.valor), 0) from LancamentoFinanceiro l "
            + "where l.encontro.id = :encontroId and l.tipo = :tipo and l.deleted = false")
    BigDecimal somarPorTipo(@Param("encontroId") UUID encontroId, @Param("tipo") TipoLancamento tipo);
}
