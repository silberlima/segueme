package com.silberlima.segueme.inscricoes.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PreInscricaoRepository extends JpaRepository<PreInscricao, UUID>, JpaSpecificationExecutor<PreInscricao> {

    /** Todo {@link StatusPreInscricao} representa uma vaga ocupada (não há estado de cancelamento). */
    long countByEncontro_Id(UUID encontroId);

    long countByEncontro_IdAndStatus(UUID encontroId, StatusPreInscricao status);

    boolean existsByPessoa_IdAndEncontro_Id(UUID pessoaId, UUID encontroId);

    @EntityGraph(attributePaths = {"pessoa", "pessoa.paroquia", "encontro"})
    Optional<PreInscricao> findByIdAndDeletedFalse(UUID id);
}
