package com.silberlima.segueme.hierarquia.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
