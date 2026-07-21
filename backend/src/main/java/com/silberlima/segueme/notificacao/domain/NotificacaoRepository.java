package com.silberlima.segueme.notificacao.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {

    Page<Notificacao> findByDestinatarioIdAndDeletedFalse(String destinatarioId, Pageable pageable);

    List<Notificacao> findByDestinatarioIdAndLidaFalseAndDeletedFalse(String destinatarioId);
}
