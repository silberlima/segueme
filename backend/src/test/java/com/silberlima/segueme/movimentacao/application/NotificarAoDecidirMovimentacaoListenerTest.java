package com.silberlima.segueme.movimentacao.application;

import static org.mockito.Mockito.verify;

import com.silberlima.segueme.movimentacao.domain.EmprestimoDecididoEvent;
import com.silberlima.segueme.movimentacao.domain.TransferenciaDecididaEvent;
import com.silberlima.segueme.notificacao.application.PublicarNotificacaoUseCase;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificarAoDecidirMovimentacaoListenerTest {

    @Mock
    private PublicarNotificacaoUseCase publicarNotificacaoUseCase;

    @InjectMocks
    private NotificarAoDecidirMovimentacaoListener listener;

    @Test
    void shouldPublishPositivaWhenTransferenciaIsApproved() {
        listener.aoDecidirTransferencia(
                new TransferenciaDecididaEvent("Maria", "São Pedro", "Santo Antônio", true, null, "solicitante"));

        verify(publicarNotificacaoUseCase)
                .execute(
                        ArgumentMatchers.eq("solicitante"),
                        ArgumentMatchers.eq(TipoNotificacao.POSITIVA),
                        ArgumentMatchers.contains("Maria"),
                        ArgumentMatchers.contains("São Pedro → Santo Antônio"));
    }

    @Test
    void shouldPublishNegativaWithJustificativaWhenTransferenciaIsRefused() {
        listener.aoDecidirTransferencia(new TransferenciaDecididaEvent(
                "Maria", "São Pedro", "Santo Antônio", false, "Pendências abertas.", "solicitante"));

        verify(publicarNotificacaoUseCase)
                .execute(
                        ArgumentMatchers.eq("solicitante"),
                        ArgumentMatchers.eq(TipoNotificacao.NEGATIVA),
                        ArgumentMatchers.contains("Maria"),
                        ArgumentMatchers.contains("Pendências abertas."));
    }

    @Test
    void shouldPublishNegativaWithJustificativaWhenEmprestimoIsRefused() {
        listener.aoDecidirEmprestimo(
                new EmprestimoDecididoEvent("Cozinha", 12, 0, true, "Sem gente disponível.", "montagem"));

        verify(publicarNotificacaoUseCase)
                .execute(
                        ArgumentMatchers.eq("montagem"),
                        ArgumentMatchers.eq(TipoNotificacao.NEGATIVA),
                        ArgumentMatchers.contains("Cozinha"),
                        ArgumentMatchers.contains("Sem gente disponível."));
    }

    @Test
    void shouldPublishInformativaWithCandidateCountWhenSetorialSendsCandidates() {
        listener.aoDecidirEmprestimo(new EmprestimoDecididoEvent("Cozinha", 12, 3, false, null, "montagem"));

        verify(publicarNotificacaoUseCase)
                .execute(
                        ArgumentMatchers.eq("montagem"),
                        ArgumentMatchers.eq(TipoNotificacao.INFORMATIVA),
                        ArgumentMatchers.contains("Cozinha"),
                        ArgumentMatchers.contains("3 candidato(s)"));
    }
}
