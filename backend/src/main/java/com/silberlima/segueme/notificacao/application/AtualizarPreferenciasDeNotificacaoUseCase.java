package com.silberlima.segueme.notificacao.application;

import com.silberlima.segueme.notificacao.domain.PreferenciaNotificacao;
import com.silberlima.segueme.notificacao.domain.PreferenciaNotificacaoRepository;
import com.silberlima.segueme.notificacao.domain.PreferenciasDeNotificacao;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Grava as preferências de notificação do próprio usuário autenticado (RF-057).
 *
 * <p>Quem pode alterar as preferências de <em>outro</em> usuário é a pendência #34 de
 * {@code docs/pendencias-gerais.md}: enquanto não houver decisão do PO, o {@code usuarioId} vem
 * sempre do {@code sub} do token e não existe caminho de administração global — a alternativa
 * (abrir um endpoint por usuário) criaria um poder que ninguém validou.
 */
@Service
public class AtualizarPreferenciasDeNotificacaoUseCase {

    private final PreferenciaNotificacaoRepository preferenciaNotificacaoRepository;

    public AtualizarPreferenciasDeNotificacaoUseCase(
            PreferenciaNotificacaoRepository preferenciaNotificacaoRepository) {
        this.preferenciaNotificacaoRepository = preferenciaNotificacaoRepository;
    }

    @Transactional
    public PreferenciasDeNotificacao execute(String usuarioId, Map<TipoNotificacao, Boolean> escolhas) {
        escolhas.forEach((tipo, ativo) -> gravar(usuarioId, tipo, ativo));
        return PreferenciasDeNotificacao.de(
                preferenciaNotificacaoRepository.findByUsuarioIdAndDeletedFalse(usuarioId));
    }

    private void gravar(String usuarioId, TipoNotificacao tipo, boolean ativo) {
        PreferenciaNotificacao preferencia = preferenciaNotificacaoRepository
                .findByUsuarioIdAndTipoAndDeletedFalse(usuarioId, tipo)
                .orElseGet(() -> new PreferenciaNotificacao(usuarioId, tipo, ativo));
        preferencia.definir(ativo);
        preferenciaNotificacaoRepository.save(preferencia);
    }
}
