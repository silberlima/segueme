package com.silberlima.segueme.montagem.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegranteEquipeRepository extends JpaRepository<IntegranteEquipe, UUID> {

    @EntityGraph(attributePaths = {"pessoa"})
    List<IntegranteEquipe> findByEncontro_IdAndEquipe_Id(UUID encontroId, UUID equipeId);

    @EntityGraph(attributePaths = {"pessoa", "equipe"})
    List<IntegranteEquipe> findByEncontro_Id(UUID encontroId);

    Optional<IntegranteEquipe> findByEncontro_IdAndEquipe_IdAndPessoa_IdAndStatusNot(
            UUID encontroId, UUID equipeId, UUID pessoaId, StatusIntegrante status);

    long countByEncontro_IdAndEquipe_IdAndStatusNot(UUID encontroId, UUID equipeId, StatusIntegrante status);

    /** Ids de pessoa já vinculadas (não recusadas) a esta equipe/encontro — para excluir do pool de candidatos. */
    @org.springframework.data.jpa.repository.Query(
            "select i.pessoa.id from IntegranteEquipe i where i.encontro.id = :encontroId "
                    + "and i.equipe.id = :equipeId and i.status <> com.silberlima.segueme.montagem.domain.StatusIntegrante.RECUSADO")
    Set<UUID> idsPessoaVinculadaNaEquipe(UUID encontroId, UUID equipeId);

    /** Participações anteriores da pessoa no mesmo tipo de equipe, em outros Encontros (RN-09: histórico). */
    long countByPessoa_IdAndEquipe_IdAndEncontro_IdNotAndStatusNot(
            UUID pessoaId, UUID equipeId, UUID encontroId, StatusIntegrante status);

    /** Total de participações anteriores da pessoa em qualquer equipe, em outros Encontros (cumprimento de pré-requisito). */
    long countByPessoa_IdAndEncontro_IdNotAndStatusNot(UUID pessoaId, UUID encontroId, StatusIntegrante status);
}
