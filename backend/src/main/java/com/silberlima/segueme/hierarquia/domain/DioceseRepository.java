package com.silberlima.segueme.hierarquia.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DioceseRepository extends JpaRepository<Diocese, UUID> {

    Page<Diocese> findByDeletedFalse(Pageable pageable);

    Optional<Diocese> findByIdAndDeletedFalse(UUID id);

    /* KPIs e ranking do Painel Nacional (Fase 12, RF-053). */

    long countByDeletedFalse();

    List<Diocese> findByDeletedFalseOrderByNomeAsc();
}
