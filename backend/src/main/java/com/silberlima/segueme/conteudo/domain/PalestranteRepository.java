package com.silberlima.segueme.conteudo.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PalestranteRepository extends JpaRepository<Palestrante, UUID>, JpaSpecificationExecutor<Palestrante> {

    @EntityGraph(attributePaths = {"pessoa"})
    Optional<Palestrante> findByIdAndDeletedFalse(UUID id);
}
