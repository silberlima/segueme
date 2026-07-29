package com.silberlima.segueme.posencontro.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FrequenciaPosEncontroRepository extends JpaRepository<FrequenciaPosEncontro, UUID> {

    @EntityGraph(attributePaths = {"pessoa"})
    List<FrequenciaPosEncontro> findByEncontroPos_IdAndDeletedFalse(UUID encontroPosId);

    Optional<FrequenciaPosEncontro> findByEncontroPos_IdAndPessoa_IdAndDeletedFalse(UUID encontroPosId, UUID pessoaId);

    /** Presenças por pessoa numa paróquia e janela de datas (RF-059, telas Frequência e Prioridades). */
    @Query("select f.pessoa.id as pessoaId, f.encontroPos.paroquia.id as paroquiaId, count(f) as presencas "
            + "from FrequenciaPosEncontro f "
            + "where f.presente = true and f.deleted = false and f.encontroPos.deleted = false "
            + "and f.encontroPos.paroquia.id = :paroquiaId and f.encontroPos.data between :de and :ate "
            + "group by f.pessoa.id, f.encontroPos.paroquia.id")
    List<ContagemPresencas> contarPresencasNaParoquia(
            @Param("paroquiaId") UUID paroquiaId, @Param("de") LocalDate de, @Param("ate") LocalDate ate);

    /**
     * Mesma apuração sem recorte de paróquia, numa única consulta: o motor de sugestões da Fase 6
     * avalia o pool nacional de candidatos e não pode disparar uma consulta por pessoa
     * (`skills/05-persistencia.md`, N+1).
     */
    @Query("select f.pessoa.id as pessoaId, f.encontroPos.paroquia.id as paroquiaId, count(f) as presencas "
            + "from FrequenciaPosEncontro f "
            + "where f.presente = true and f.deleted = false and f.encontroPos.deleted = false "
            + "and f.encontroPos.data between :de and :ate "
            + "group by f.pessoa.id, f.encontroPos.paroquia.id")
    List<ContagemPresencas> contarPresencas(@Param("de") LocalDate de, @Param("ate") LocalDate ate);
}
