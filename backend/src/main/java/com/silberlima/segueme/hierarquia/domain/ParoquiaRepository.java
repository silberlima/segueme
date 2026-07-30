package com.silberlima.segueme.hierarquia.domain;

import com.silberlima.segueme.domain.model.ContagemPorTerritorio;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParoquiaRepository extends JpaRepository<Paroquia, UUID> {

    @EntityGraph(attributePaths = {"setor", "setor.diocese"})
    Page<Paroquia> findByDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = {"setor", "setor.diocese"})
    Page<Paroquia> findBySetor_Diocese_IdAndDeletedFalse(UUID dioceseId, Pageable pageable);

    @EntityGraph(attributePaths = {"setor", "setor.diocese"})
    Page<Paroquia> findBySetor_IdAndDeletedFalse(UUID setorId, Pageable pageable);

    @EntityGraph(attributePaths = {"setor", "setor.diocese"})
    Optional<Paroquia> findByIdAndDeletedFalse(UUID id);

    List<Paroquia> findBySetor_IdInAndDeletedFalse(List<UUID> setorIds);

    boolean existsByIdAndDeletedFalse(UUID id);

    /** Território da paróquia para a checagem de escopo do {@code @PreAuthorize} (RNF-06). */
    @Query("select new com.silberlima.segueme.hierarquia.domain.Territorio("
            + "p.id, p.setor.id, p.setor.diocese.id) from Paroquia p where p.id = :id and p.deleted = false")
    Optional<Territorio> buscarTerritorio(@Param("id") UUID id);

    /* KPIs territoriais dos dashboards (Fase 12, RF-053). */

    long countByDeletedFalse();

    long countBySetor_Diocese_IdAndDeletedFalse(UUID dioceseId);

    long countBySetor_IdAndDeletedFalse(UUID setorId);

    List<Paroquia> findBySetor_IdAndDeletedFalseOrderByNomeAsc(UUID setorId);

    /** Contagem de paróquias por diocese para o ranking "Dioceses em destaque" do Painel Nacional. */
    @Query("select p.setor.diocese.id as territorioId, count(p) as total from Paroquia p "
            + "where p.deleted = false group by p.setor.diocese.id")
    List<ContagemPorTerritorio> contarPorDiocese();
}
