package com.silberlima.segueme.convites.application;

import com.silberlima.segueme.convites.domain.ConviteRecusadoEvent;
import com.silberlima.segueme.notificacao.application.PublicarNotificacaoUseCase;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reage a {@link ConviteRecusadoEvent} criando a notificação imediata prevista em RF-038/RN-12.
 * Escuta após o commit da transação de recusa, garantindo que só notifica se a recusa realmente
 * foi persistida.
 *
 * <p>Publica por {@link PublicarNotificacaoUseCase} (Fase 13) e não direto pelo repositório: é ali
 * que a preferência de notificação do destinatário (RF-057) é respeitada.
 */
@Component
public class NotificarAoRecusarConviteListener {

    private final PublicarNotificacaoUseCase publicarNotificacaoUseCase;

    public NotificarAoRecusarConviteListener(PublicarNotificacaoUseCase publicarNotificacaoUseCase) {
        this.publicarNotificacaoUseCase = publicarNotificacaoUseCase;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoRecusarConvite(ConviteRecusadoEvent evento) {
        publicarNotificacaoUseCase.execute(
                evento.destinatarioId(),
                TipoNotificacao.NEGATIVA,
                "Convite recusado: " + evento.equipeNome(),
                evento.pessoaNome() + " recusou o convite para a equipe " + evento.equipeNome()
                        + ". Justificativa: " + evento.justificativa());
    }
}
