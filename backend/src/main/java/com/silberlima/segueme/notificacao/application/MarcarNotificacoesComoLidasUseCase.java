package com.silberlima.segueme.notificacao.application;

import com.silberlima.segueme.notificacao.domain.Notificacao;
import com.silberlima.segueme.notificacao.domain.NotificacaoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarcarNotificacoesComoLidasUseCase {

    private final NotificacaoRepository notificacaoRepository;

    public MarcarNotificacoesComoLidasUseCase(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    @Transactional
    public void execute(String destinatarioId) {
        List<Notificacao> naoLidas = notificacaoRepository
                .findByDestinatarioIdAndLidaFalseAndDeletedFalse(destinatarioId);
        naoLidas.forEach(Notificacao::marcarComoLida);
        notificacaoRepository.saveAll(naoLidas);
    }
}
