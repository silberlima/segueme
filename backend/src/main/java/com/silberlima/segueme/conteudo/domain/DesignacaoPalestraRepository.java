package com.silberlima.segueme.conteudo.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignacaoPalestraRepository extends JpaRepository<DesignacaoPalestra, UUID> {

    @EntityGraph(attributePaths = {"palestra", "palestrante", "palestrante.pessoa"})
    List<DesignacaoPalestra> findByEncontro_IdAndDeletedFalse(UUID encontroId);

    Optional<DesignacaoPalestra> findByEncontro_IdAndPalestra_IdAndDeletedFalse(UUID encontroId, UUID palestraId);
}
