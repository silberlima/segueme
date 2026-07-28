package com.silberlima.segueme.convites.application;

import com.silberlima.segueme.convites.domain.ConviteRecusadoEvent;
import com.silberlima.segueme.notificacao.domain.Notificacao;
import com.silberlima.segueme.notificacao.domain.NotificacaoRepository;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reage a {@link ConviteRecusadoEvent} criando a notificação imediata prevista em RF-038/RN-12.
 * Escuta após o commit da transação de recusa, garantindo que só notifica se a recusa realmente
 * foi persistida.
 */
@Component
public class NotificarAoRecusarConviteListener {

    private final NotificacaoRepository notificacaoRepository;

    public NotificarAoRecusarConviteListener(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoRecusarConvite(ConviteRecusadoEvent evento) {
        if (evento.destinatarioId() == null) {
            return;
        }
        notificacaoRepository.save(new Notificacao(
                evento.destinatarioId(),
                TipoNotificacao.NEGATIVA,
                "Convite recusado: " + evento.equipeNome(),
                evento.pessoaNome() + " recusou o convite para a equipe " + evento.equipeNome()
                        + ". Justificativa: " + evento.justificativa()));
    }
}
