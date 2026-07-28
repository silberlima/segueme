package com.silberlima.segueme.movimentacao.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransferenciaRepository
        extends JpaRepository<Transferencia, UUID>, JpaSpecificationExecutor<Transferencia> {

    @EntityGraph(attributePaths = {"pessoa", "paroquiaOrigem", "paroquiaDestino"})
    Optional<Transferencia> findByIdAndDeletedFalse(UUID id);
}
