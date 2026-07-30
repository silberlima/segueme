package com.silberlima.segueme.notificacao.application;

import com.silberlima.segueme.notificacao.domain.PreferenciaNotificacaoRepository;
import com.silberlima.segueme.notificacao.domain.PreferenciasDeNotificacao;
import org.springframework.stereotype.Service;

/** Preferências de notificação do usuário, completas nos três tipos de RN-SISTEMA-01 (RF-057). */
@Service
public class ConsultarPreferenciasDeNotificacaoUseCase {

    private final PreferenciaNotificacaoRepository preferenciaNotificacaoRepository;

    public ConsultarPreferenciasDeNotificacaoUseCase(
            PreferenciaNotificacaoRepository preferenciaNotificacaoRepository) {
        this.preferenciaNotificacaoRepository = preferenciaNotificacaoRepository;
    }

    public PreferenciasDeNotificacao execute(String usuarioId) {
        return PreferenciasDeNotificacao.de(
                preferenciaNotificacaoRepository.findByUsuarioIdAndDeletedFalse(usuarioId));
    }
}
