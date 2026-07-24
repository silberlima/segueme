package com.silberlima.segueme.encontros.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipeDirigenteRepository extends JpaRepository<EquipeDirigente, UUID> {

    Optional<EquipeDirigente> findByEncontro_Id(UUID encontroId);
}
