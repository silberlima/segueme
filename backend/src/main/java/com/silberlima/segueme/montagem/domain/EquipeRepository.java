package com.silberlima.segueme.montagem.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipeRepository extends JpaRepository<Equipe, UUID> {

    List<Equipe> findByDeletedFalseOrderByNomeAsc();

    @EntityGraph(attributePaths = {"competenciasRequeridas"})
    Optional<Equipe> findByIdAndDeletedFalse(UUID id);
}
