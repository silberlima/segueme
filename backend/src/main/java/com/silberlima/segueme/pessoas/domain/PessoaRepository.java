package com.silberlima.segueme.pessoas.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PessoaRepository extends JpaRepository<Pessoa, UUID>, JpaSpecificationExecutor<Pessoa> {

    @EntityGraph(attributePaths = {"paroquia", "indicadoPor", "competencias"})
    Optional<Pessoa> findByIdAndDeletedFalse(UUID id);

    boolean existsByIdAndDeletedFalse(UUID id);

    /**
     * Pool de candidatos a integrante de equipe (`montagem-de-equipes.md`, Fase 6): só quem já
     * concluiu um Encontro como participante ({@link SituacaoPessoa#SEGUIDOR}) serve em equipes de
     * serviço — modelagem adotada na ausência de um RN explícito sobre o critério de elegibilidade.
     */
    @EntityGraph(attributePaths = {"paroquia", "competencias"})
    List<Pessoa> findBySituacaoAndDeletedFalse(SituacaoPessoa situacao);

    @Query("select comp.id as competenciaId, count(p) as total "
            + "from Pessoa p join p.competencias comp where p.deleted = false group by comp.id")
    List<ContagemPorCompetencia> contarPorCompetencia();

    /**
     * Inicializa a coleção {@code competencias} das pessoas informadas (por id) numa única
     * consulta em lote, dentro da transação chamadora — evita tanto N+1 (`skills/05-persistencia.md`)
     * quanto {@code LazyInitializationException} ao mapear a resposta fora da sessão, já que
     * {@code open-in-view} está desligado (`application.yml`) e o fetch join de coleção não pode
     * ser combinado com paginação na consulta principal.
     */
    @Query("select distinct p from Pessoa p left join fetch p.competencias where p.id in :pessoaIds")
    List<Pessoa> inicializarCompetencias(@Param("pessoaIds") List<UUID> pessoaIds);
}
