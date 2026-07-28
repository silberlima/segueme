package com.silberlima.segueme.montagem.domain;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubstituicaoRepository extends JpaRepository<Substituicao, UUID> {

    @EntityGraph(attributePaths = {"equipe", "pessoaSai", "pessoaEntra"})
    Page<Substituicao> findByEncontro_IdOrderByDataSubstituicaoDesc(UUID encontroId, Pageable pageable);

    @EntityGraph(attributePaths = {"equipe", "pessoaSai", "pessoaEntra"})
    Page<Substituicao> findByEncontro_IdAndEquipe_IdOrderByDataSubstituicaoDesc(
            UUID encontroId, UUID equipeId, Pageable pageable);
}
