package com.silberlima.segueme.montagem.application;

import com.silberlima.segueme.montagem.domain.SubstituicaoRegistradaEvent;
import com.silberlima.segueme.notificacao.application.PublicarNotificacaoUseCase;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Notificação automática da substituição de integrante (RNF-07). Escuta após o commit para só
 * notificar a troca que de fato foi persistida.
 */
@Component
public class NotificarAoRegistrarSubstituicaoListener {

    private final PublicarNotificacaoUseCase publicarNotificacaoUseCase;

    public NotificarAoRegistrarSubstituicaoListener(PublicarNotificacaoUseCase publicarNotificacaoUseCase) {
        this.publicarNotificacaoUseCase = publicarNotificacaoUseCase;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoRegistrarSubstituicao(SubstituicaoRegistradaEvent evento) {
        publicarNotificacaoUseCase.execute(
                evento.destinatarioId(),
                TipoNotificacao.INFORMATIVA,
                "Substituição em " + evento.equipeNome(),
                evento.pessoaSaiNome() + " saiu e " + evento.pessoaEntraNome() + " entrou na equipe "
                        + evento.equipeNome() + " do Encontro #" + evento.numeroDoEncontro()
                        + ". Justificativa: " + evento.justificativa());
    }
}
