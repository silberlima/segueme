package com.silberlima.segueme.movimentacao.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, UUID>, JpaSpecificationExecutor<Emprestimo> {

    @EntityGraph(attributePaths = {"encontro", "encontro.paroquia", "equipe", "competenciaRequerida"})
    Optional<Emprestimo> findByIdAndDeletedFalse(UUID id);
}
