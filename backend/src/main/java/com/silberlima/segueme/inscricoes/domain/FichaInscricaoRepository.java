package com.silberlima.segueme.inscricoes.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FichaInscricaoRepository extends JpaRepository<FichaInscricao, UUID>, JpaSpecificationExecutor<FichaInscricao> {

    boolean existsByPreInscricao_Id(UUID preInscricaoId);

    long countByEncontro_Id(UUID encontroId);

    @EntityGraph(attributePaths = {"pessoa", "pessoa.paroquia"})
    Page<FichaInscricao> findByEncontro_Id(UUID encontroId, Pageable pageable);

    @EntityGraph(attributePaths = {"pessoa", "pessoa.paroquia", "encontro"})
    Optional<FichaInscricao> findByIdAndDeletedFalse(UUID id);
}
