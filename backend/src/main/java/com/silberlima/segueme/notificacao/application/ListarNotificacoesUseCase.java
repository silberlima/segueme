package com.silberlima.segueme.notificacao.application;

import com.silberlima.segueme.notificacao.domain.Notificacao;
import com.silberlima.segueme.notificacao.domain.NotificacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ListarNotificacoesUseCase {

    private final NotificacaoRepository notificacaoRepository;

    public ListarNotificacoesUseCase(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    public Page<Notificacao> execute(String destinatarioId, Pageable pageable) {
        return notificacaoRepository.findByDestinatarioIdAndDeletedFalse(destinatarioId, pageable);
    }
}
