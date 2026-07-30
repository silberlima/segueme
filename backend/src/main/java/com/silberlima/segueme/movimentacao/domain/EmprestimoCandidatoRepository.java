package com.silberlima.segueme.movimentacao.domain;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmprestimoCandidatoRepository extends JpaRepository<EmprestimoCandidato, UUID> {

    @EntityGraph(attributePaths = {"pessoa", "pessoa.paroquia"})
    List<EmprestimoCandidato> findByEmprestimo_Id(UUID emprestimoId);

    /** Quantos candidatos o Setorial já enviou — vai no texto da notificação à Montagem (RNF-07). */
    int countByEmprestimo_Id(UUID emprestimoId);

    List<EmprestimoCandidato> findByEmprestimo_IdAndStatus(UUID emprestimoId, StatusCandidatoEmprestimo status);

    boolean existsByEmprestimo_IdAndPessoa_Id(UUID emprestimoId, UUID pessoaId);

    @Query("select c.pessoa.id from EmprestimoCandidato c where c.emprestimo.id = :emprestimoId")
    Set<UUID> idsPessoaCandidataAoEmprestimo(UUID emprestimoId);
}
