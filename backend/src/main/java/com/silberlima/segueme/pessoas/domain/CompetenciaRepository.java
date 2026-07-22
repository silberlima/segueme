package com.silberlima.segueme.pessoas.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetenciaRepository extends JpaRepository<Competencia, UUID> {

    List<Competencia> findByDeletedFalseOrderByNomeAsc();

    Optional<Competencia> findByIdAndDeletedFalse(UUID id);
}
