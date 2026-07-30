package com.silberlima.segueme.notificacao.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenciaNotificacaoRepository extends JpaRepository<PreferenciaNotificacao, UUID> {

    List<PreferenciaNotificacao> findByUsuarioIdAndDeletedFalse(String usuarioId);

    Optional<PreferenciaNotificacao> findByUsuarioIdAndTipoAndDeletedFalse(String usuarioId, TipoNotificacao tipo);
}
