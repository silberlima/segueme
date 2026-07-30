package com.silberlima.segueme.encontros.domain;

import com.silberlima.segueme.domain.model.ContagemPorTerritorio;
import com.silberlima.segueme.hierarquia.domain.Territorio;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EncontroRepository extends JpaRepository<Encontro, UUID>, JpaSpecificationExecutor<Encontro> {

    /** Base para o próximo número sequencial do Encontro na paróquia (RN-16). */
    Optional<Encontro> findTopByParoquia_IdOrderByNumeroDesc(UUID paroquiaId);

    @EntityGraph(attributePaths = {"paroquia", "diretorEspiritual"})
    Optional<Encontro> findByIdAndDeletedFalse(UUID id);

    /** Território do Encontro para a checagem de escopo do {@code @PreAuthorize} (RNF-06). */
    @Query("select new com.silberlima.segueme.hierarquia.domain.Territorio("
            + "e.paroquia.id, e.paroquia.setor.id, e.paroquia.setor.diocese.id) "
            + "from Encontro e where e.id = :id and e.deleted = false")
    Optional<Territorio> buscarTerritorio(@Param("id") UUID id);

    /* Agregações dos dashboards por perfil (Fase 12, RF-053). */

    long countByDataInicioBetweenAndDeletedFalse(LocalDate de, LocalDate ate);

    long countByParoquia_Setor_Diocese_IdAndDataInicioBetweenAndDeletedFalse(
            UUID dioceseId, LocalDate de, LocalDate ate);

    long countByParoquia_IdAndStatusAndDeletedFalse(UUID paroquiaId, StatusEncontro status);

    /**
     * Próximo Encontro da paróquia — o mais próximo que ainda não terminou. É o Encontro "corrente"
     * de todos os painéis de equipe (Fichas, Montagem, Comando, Finanças), que no protótipo sempre
     * falam de um único Encontro em preparação, sem seletor na tela.
     */
    @EntityGraph(attributePaths = {"paroquia", "diretorEspiritual"})
    Optional<Encontro> findTopByParoquia_IdAndStatusAndDataTerminoGreaterThanEqualAndDeletedFalseOrderByDataInicioAsc(
            UUID paroquiaId, StatusEncontro status, LocalDate dataTermino);

    /** Fallback quando a paróquia não tem Encontro em aberto: o último realizado. */
    @EntityGraph(attributePaths = {"paroquia", "diretorEspiritual"})
    Optional<Encontro> findTopByParoquia_IdAndDeletedFalseOrderByDataInicioDesc(UUID paroquiaId);

    /** Tabela "Encontros recentes" do Painel da Paróquia (recorte top N, sem paginação). */
    @EntityGraph(attributePaths = {"paroquia", "diretorEspiritual"})
    List<Encontro> findByParoquia_IdAndDeletedFalseOrderByDataInicioDesc(UUID paroquiaId, Pageable pageable);

    /** Gráfico "Encontros por região" do Painel Nacional; dioceses sem região caem em {@code null}. */
    @Query("select e.paroquia.setor.diocese.regiao as regiao, count(e) as total from Encontro e "
            + "where e.deleted = false and e.dataInicio between :de and :ate "
            + "group by e.paroquia.setor.diocese.regiao")
    List<ContagemPorRegiao> contarPorRegiao(@Param("de") LocalDate de, @Param("ate") LocalDate ate);

    /** Data do próximo Encontro de cada paróquia do setor, numa consulta só (lista do Painel do Setor). */
    @Query("select e.paroquia.id as territorioId, min(e.dataInicio) as data from Encontro e "
            + "where e.deleted = false and e.status = com.silberlima.segueme.encontros.domain.StatusEncontro.ATIVO "
            + "and e.dataTermino >= :hoje and e.paroquia.setor.id = :setorId group by e.paroquia.id")
    List<ProximoEncontroDaParoquia> proximoEncontroPorParoquiaDoSetor(
            @Param("setorId") UUID setorId, @Param("hoje") LocalDate hoje);

    /** Encontros por diocese no período — insumo do ranking "Dioceses em destaque" do Painel Nacional. */
    @Query("select e.paroquia.setor.diocese.id as territorioId, count(e) as total from Encontro e "
            + "where e.deleted = false and e.dataInicio between :de and :ate "
            + "group by e.paroquia.setor.diocese.id")
    List<ContagemPorTerritorio> contarPorDiocese(@Param("de") LocalDate de, @Param("ate") LocalDate ate);
}
