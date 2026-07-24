package com.silberlima.segueme.encontros.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembroEquipeDirigenteRepository extends JpaRepository<MembroEquipeDirigente, UUID> {

    @EntityGraph(attributePaths = {"pessoa", "casal", "casal.pessoa1", "casal.pessoa2"})
    List<MembroEquipeDirigente> findByEquipeDirigente_Encontro_Id(UUID encontroId);
}
