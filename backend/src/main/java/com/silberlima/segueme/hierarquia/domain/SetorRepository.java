package com.silberlima.segueme.hierarquia.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SetorRepository extends JpaRepository<Setor, UUID> {

    List<Setor> findByDiocese_IdInAndDeletedFalse(List<UUID> dioceseIds);

    Optional<Setor> findByIdAndDeletedFalse(UUID id);

    /* KPIs do Painel da Diocese (Fase 12, RF-053). */

    long countByDiocese_IdAndDeletedFalse(UUID dioceseId);

    List<Setor> findByDiocese_IdAndDeletedFalseOrderByNomeAsc(UUID dioceseId);
}
