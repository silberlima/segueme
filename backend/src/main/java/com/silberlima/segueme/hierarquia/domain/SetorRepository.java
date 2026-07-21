package com.silberlima.segueme.hierarquia.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SetorRepository extends JpaRepository<Setor, UUID> {

    List<Setor> findByDiocese_IdInAndDeletedFalse(List<UUID> dioceseIds);
}
