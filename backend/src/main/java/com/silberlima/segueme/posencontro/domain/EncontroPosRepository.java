package com.silberlima.segueme.posencontro.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EncontroPosRepository extends JpaRepository<EncontroPos, UUID> {

    @EntityGraph(attributePaths = {"paroquia"})
    Optional<EncontroPos> findByIdAndDeletedFalse(UUID id);

    @EntityGraph(attributePaths = {"paroquia"})
    Page<EncontroPos> findByParoquia_IdAndDeletedFalseOrderByDataDesc(UUID paroquiaId, Pageable pageable);

    boolean existsByParoquia_IdAndDataAndDeletedFalse(UUID paroquiaId, LocalDate data);

    /** Denominador das faixas de prioridade: quantos encontros pós a paróquia realizou na janela apurada (RN-POS-03). */
    long countByParoquia_IdAndDataBetweenAndDeletedFalse(UUID paroquiaId, LocalDate de, LocalDate ate);

    /** Mesmo denominador, por paróquia, para apurar todas de uma vez sem N+1 (usado pelo motor de sugestões da Fase 6). */
    @Query("select e.paroquia.id as paroquiaId, count(e) as total from EncontroPos e "
            + "where e.deleted = false and e.data between :de and :ate group by e.paroquia.id")
    List<ContagemEncontrosPos> contarPorParoquia(@Param("de") LocalDate de, @Param("ate") LocalDate ate);
}
