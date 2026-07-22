package com.silberlima.segueme.pessoas.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoEventoRepository extends JpaRepository<HistoricoEvento, UUID> {

    List<HistoricoEvento> findByPessoa_IdOrderByDataEventoAsc(UUID pessoaId);
}
