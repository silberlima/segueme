package com.silberlima.segueme.relatorios.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RelatorioRepository extends JpaRepository<Relatorio, UUID> {

    /**
     * "Gerados recentemente" do solicitante. Projeta sem a coluna {@code conteudo}: a lista mostra
     * metadados, e trazer o CSV inteiro de cada relatório numa listagem seria carregar dado que a
     * tela não usa (skills/10 — selecionar apenas as colunas necessárias).
     */
    @Query("select new com.silberlima.segueme.relatorios.domain.RelatorioResumo("
            + "r.id, r.tipo, r.status, r.totalLinhas, r.erro, r.createdAt, r.geradoEm) "
            + "from Relatorio r where r.solicitanteId = :solicitanteId and r.deleted = false")
    Page<RelatorioResumo> resumosDoSolicitante(
            @Param("solicitanteId") String solicitanteId, Pageable pageable);

    Optional<Relatorio> findByIdAndDeletedFalse(UUID id);
}
