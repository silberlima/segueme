package com.silberlima.segueme.encontros.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EncontroRepository extends JpaRepository<Encontro, UUID>, JpaSpecificationExecutor<Encontro> {

    /** Base para o próximo número sequencial do Encontro na paróquia (RN-16). */
    Optional<Encontro> findTopByParoquia_IdOrderByNumeroDesc(UUID paroquiaId);

    @EntityGraph(attributePaths = {"paroquia", "diretorEspiritual"})
    Optional<Encontro> findByIdAndDeletedFalse(UUID id);
}
