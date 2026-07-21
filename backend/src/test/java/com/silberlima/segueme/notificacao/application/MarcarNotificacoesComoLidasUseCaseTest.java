package com.silberlima.segueme.notificacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.notificacao.domain.Notificacao;
import com.silberlima.segueme.notificacao.domain.NotificacaoRepository;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarcarNotificacoesComoLidasUseCaseTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Test
    void shouldMarkAllUnreadNotificationsAsRead() {
        String destinatarioId = "user-1";
        Notificacao naoLida = new Notificacao(destinatarioId, TipoNotificacao.POSITIVA, "Convite aceito", "Mensagem");
        when(notificacaoRepository.findByDestinatarioIdAndLidaFalseAndDeletedFalse(destinatarioId))
                .thenReturn(List.of(naoLida));

        new MarcarNotificacoesComoLidasUseCase(notificacaoRepository).execute(destinatarioId);

        assertThat(naoLida.isLida()).isTrue();
        assertThat(naoLida.getLidaEm()).isNotNull();
        ArgumentCaptor<List<Notificacao>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificacaoRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).containsExactly(naoLida);
    }

    @Test
    void shouldDoNothingWhenNoUnreadNotifications() {
        String destinatarioId = "user-2";
        when(notificacaoRepository.findByDestinatarioIdAndLidaFalseAndDeletedFalse(destinatarioId))
                .thenReturn(List.of());

        new MarcarNotificacoesComoLidasUseCase(notificacaoRepository).execute(destinatarioId);

        verify(notificacaoRepository).saveAll(List.of());
    }
}
