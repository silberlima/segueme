package com.silberlima.segueme.financeiro.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CampanhaRepository extends JpaRepository<Campanha, UUID>, JpaSpecificationExecutor<Campanha> {

    @EntityGraph(attributePaths = {"encontro"})
    Optional<Campanha> findByIdAndDeletedFalse(UUID id);

    long countByEncontro_IdAndStatusAndDeletedFalse(UUID encontroId, StatusCampanha status);
}
