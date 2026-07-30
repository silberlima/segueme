package com.silberlima.segueme.movimentacao.application;

import com.silberlima.segueme.movimentacao.domain.EmprestimoDecididoEvent;
import com.silberlima.segueme.movimentacao.domain.TransferenciaDecididaEvent;
import com.silberlima.segueme.notificacao.application.PublicarNotificacaoUseCase;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Notificações automáticas das movimentações de vínculo — transferência e empréstimo (RNF-07).
 * Escuta após o commit para só notificar o que de fato foi persistido, e publica por
 * {@link PublicarNotificacaoUseCase}, único ponto onde a preferência do destinatário (RF-057) é
 * respeitada.
 */
@Component
public class NotificarAoDecidirMovimentacaoListener {

    private final PublicarNotificacaoUseCase publicarNotificacaoUseCase;

    public NotificarAoDecidirMovimentacaoListener(PublicarNotificacaoUseCase publicarNotificacaoUseCase) {
        this.publicarNotificacaoUseCase = publicarNotificacaoUseCase;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoDecidirTransferencia(TransferenciaDecididaEvent evento) {
        String rota = evento.paroquiaOrigemNome() + " → " + evento.paroquiaDestinoNome();
        if (evento.aprovada()) {
            publicarNotificacaoUseCase.execute(
                    evento.destinatarioId(),
                    TipoNotificacao.POSITIVA,
                    "Transferência aprovada: " + evento.pessoaNome(),
                    "O Setorial aprovou a transferência de " + evento.pessoaNome() + " (" + rota + ").");
            return;
        }
        publicarNotificacaoUseCase.execute(
                evento.destinatarioId(),
                TipoNotificacao.NEGATIVA,
                "Transferência recusada: " + evento.pessoaNome(),
                "O Setorial recusou a transferência de " + evento.pessoaNome() + " (" + rota
                        + "). Justificativa: " + evento.justificativaRecusa());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoDecidirEmprestimo(EmprestimoDecididoEvent evento) {
        String equipeDoEncontro = evento.equipeNome() + " · Encontro #" + evento.numeroDoEncontro();
        if (evento.recusado()) {
            publicarNotificacaoUseCase.execute(
                    evento.destinatarioId(),
                    TipoNotificacao.NEGATIVA,
                    "Empréstimo recusado: " + evento.equipeNome(),
                    "O Setorial recusou o empréstimo para " + equipeDoEncontro + ". Justificativa: "
                            + evento.justificativaRecusa());
            return;
        }
        publicarNotificacaoUseCase.execute(
                evento.destinatarioId(),
                TipoNotificacao.INFORMATIVA,
                "Candidatos disponíveis: " + evento.equipeNome(),
                "O Setorial enviou " + evento.candidatosEnviados() + " candidato(s) para " + equipeDoEncontro + ".");
    }
}
