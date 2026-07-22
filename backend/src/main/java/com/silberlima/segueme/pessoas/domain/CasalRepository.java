package com.silberlima.segueme.pessoas.domain;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CasalRepository extends JpaRepository<Casal, UUID> {

    boolean existsByPessoa1_IdOrPessoa2_Id(UUID pessoa1Id, UUID pessoa2Id);

    @EntityGraph(attributePaths = {"pessoa1", "pessoa1.paroquia", "pessoa2", "pessoa2.paroquia"})
    Page<Casal> findByDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = {"pessoa1", "pessoa1.paroquia", "pessoa2", "pessoa2.paroquia"})
    Page<Casal> findByPessoa1_Paroquia_Setor_Diocese_IdAndDeletedFalse(UUID dioceseId, Pageable pageable);

    @EntityGraph(attributePaths = {"pessoa1", "pessoa1.paroquia", "pessoa2", "pessoa2.paroquia"})
    Page<Casal> findByPessoa1_Paroquia_Setor_IdAndDeletedFalse(UUID setorId, Pageable pageable);

    @EntityGraph(attributePaths = {"pessoa1", "pessoa1.paroquia", "pessoa2", "pessoa2.paroquia"})
    Page<Casal> findByPessoa1_Paroquia_IdAndDeletedFalse(UUID paroquiaId, Pageable pageable);
}
