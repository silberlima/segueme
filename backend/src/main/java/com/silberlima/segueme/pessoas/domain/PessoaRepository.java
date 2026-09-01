package com.silberlima.segueme.pessoas.domain;

import com.silberlima.segueme.domain.model.ContagemPorTerritorio;
import java.util.Collection;
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

    /** A Pessoa que o usuário autenticado é — base de "meus dados" do perfil Seguidor. */
    @EntityGraph(attributePaths = {"paroquia", "indicadoPor", "competencias"})
    Optional<Pessoa> findByUsuarioIdAndDeletedFalse(String usuarioId);

    boolean existsByIdAndDeletedFalse(UUID id);

    /**
     * Guarda da unicidade do CPF (RN-PESSOA-01) no cadastro. A constraint do banco é a rede de
     * segurança; esta consulta é o que transforma a colisão numa mensagem que a equipe entende, em
     * vez de um erro de integridade.
     */
    boolean existsByCpfAndDeletedFalse(String cpf);

    /** Mesma checagem, ignorando a própria pessoa — usada quando ela corrige o próprio CPF. */
    boolean existsByCpfAndIdNotAndDeletedFalse(String cpf, UUID id);

    /**
     * Pool de candidatos a integrante de equipe (`montagem-de-equipes.md`, Fase 6): só quem já
     * concluiu um Encontro como participante ({@link SituacaoPessoa#SEGUIDOR}) serve em equipes de
     * serviço — modelagem adotada na ausência de um RN explícito sobre o critério de elegibilidade.
     */
    @EntityGraph(attributePaths = {"paroquia", "competencias"})
    List<Pessoa> findBySituacaoAndDeletedFalse(SituacaoPessoa situacao);

    /**
     * Pool de acompanhados do pós-encontro (Fase 11): quem já fez o Encontro na paróquia — tanto
     * {@link SituacaoPessoa#SEGUIMISTA} quanto {@link SituacaoPessoa#SEGUIDOR}, para que quem já
     * passou a servir em equipes continue no ranking de frequência que alimenta o motor de
     * sugestões da Fase 6.
     */
    List<Pessoa> findByParoquia_IdAndSituacaoInAndDeletedFalseOrderByNomeCompletoAsc(
            UUID paroquiaId, Collection<SituacaoPessoa> situacoes);

    @Query("select comp.id as competenciaId, count(p) as total "
            + "from Pessoa p join p.competencias comp where p.deleted = false group by comp.id")
    List<ContagemPorCompetencia> contarPorCompetencia();

    /* KPIs de pessoas dos dashboards por perfil (Fase 12, RF-053), um nível territorial por método. */

    long countBySituacaoAndDeletedFalse(SituacaoPessoa situacao);

    long countByParoquia_Setor_Diocese_IdAndSituacaoAndDeletedFalse(UUID dioceseId, SituacaoPessoa situacao);

    long countByParoquia_Setor_IdAndSituacaoAndDeletedFalse(UUID setorId, SituacaoPessoa situacao);

    long countByParoquia_IdAndSituacaoAndDeletedFalse(UUID paroquiaId, SituacaoPessoa situacao);

    /** Ranking "Dioceses em destaque" do Painel Nacional: uma consulta agrupada, nunca uma por diocese. */
    @Query("select p.paroquia.setor.diocese.id as territorioId, count(p) as total from Pessoa p "
            + "where p.deleted = false and p.situacao = :situacao group by p.paroquia.setor.diocese.id")
    List<ContagemPorTerritorio> contarPorDiocese(@Param("situacao") SituacaoPessoa situacao);

    /** Gráfico "Seguidores por setor" do Painel da Diocese. */
    @Query("select p.paroquia.setor.id as territorioId, count(p) as total from Pessoa p "
            + "where p.deleted = false and p.situacao = :situacao "
            + "and p.paroquia.setor.diocese.id = :dioceseId group by p.paroquia.setor.id")
    List<ContagemPorTerritorio> contarPorSetorDaDiocese(
            @Param("dioceseId") UUID dioceseId, @Param("situacao") SituacaoPessoa situacao);

    /** Lista "Paróquias do setor" do Painel do Setor. */
    @Query("select p.paroquia.id as territorioId, count(p) as total from Pessoa p "
            + "where p.deleted = false and p.situacao = :situacao "
            + "and p.paroquia.setor.id = :setorId group by p.paroquia.id")
    List<ContagemPorTerritorio> contarPorParoquiaDoSetor(
            @Param("setorId") UUID setorId, @Param("situacao") SituacaoPessoa situacao);

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
