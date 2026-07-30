package com.silberlima.segueme.notificacao.application;

import com.silberlima.segueme.notificacao.domain.NotificacaoRepository;
import org.springframework.stereotype.Service;

/**
 * Total de não lidas do usuário (RN-SISTEMA-02). É uma contagem no banco, e não o tamanho da
 * primeira página da listagem: o badge do sino tem que estar certo mesmo com mais notificações
 * do que cabem na página que o drawer carrega.
 */
@Service
public class ContarNotificacoesNaoLidasUseCase {

    private final NotificacaoRepository notificacaoRepository;

    public ContarNotificacoesNaoLidasUseCase(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    public long execute(String destinatarioId) {
        return notificacaoRepository.countByDestinatarioIdAndLidaFalseAndDeletedFalse(destinatarioId);
    }
}
