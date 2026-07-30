package com.silberlima.segueme.montagem.application;

import static org.mockito.Mockito.verify;

import com.silberlima.segueme.montagem.domain.SubstituicaoRegistradaEvent;
import com.silberlima.segueme.notificacao.application.PublicarNotificacaoUseCase;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificarAoRegistrarSubstituicaoListenerTest {

    @Mock
    private PublicarNotificacaoUseCase publicarNotificacaoUseCase;

    @InjectMocks
    private NotificarAoRegistrarSubstituicaoListener listener;

    @Test
    void shouldNotifyWhoPutTheOutgoingMemberInTheTeam() {
        listener.aoRegistrarSubstituicao(
                new SubstituicaoRegistradaEvent("Cozinha", 12, "Maria", "João", "Mudou de cidade.", "comando"));

        verify(publicarNotificacaoUseCase)
                .execute(
                        ArgumentMatchers.eq("comando"),
                        ArgumentMatchers.eq(TipoNotificacao.INFORMATIVA),
                        ArgumentMatchers.contains("Cozinha"),
                        ArgumentMatchers.contains("Maria"));
    }
}
