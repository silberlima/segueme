package com.silberlima.segueme.notificacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.notificacao.domain.Notificacao;
import com.silberlima.segueme.notificacao.domain.NotificacaoRepository;
import com.silberlima.segueme.notificacao.domain.PreferenciaNotificacao;
import com.silberlima.segueme.notificacao.domain.PreferenciasDeNotificacao;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Cobre a porta única de entrada da central de notificações e o respeito à preferência (RF-057). */
@ExtendWith(MockitoExtension.class)
class PublicarNotificacaoUseCaseTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private ConsultarPreferenciasDeNotificacaoUseCase consultarPreferenciasDeNotificacaoUseCase;

    @InjectMocks
    private PublicarNotificacaoUseCase publicarNotificacaoUseCase;

    @Test
    void shouldPublicarQuandoOTipoEstaAtivo() {
        when(consultarPreferenciasDeNotificacaoUseCase.execute("user-a"))
                .thenReturn(PreferenciasDeNotificacao.de(List.of()));
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(chamada -> chamada.getArgument(0));

        Optional<Notificacao> publicada = publicarNotificacaoUseCase.execute(
                "user-a", TipoNotificacao.POSITIVA, "Relatório pronto", "Mensagem");

        assertThat(publicada).isPresent();
        assertThat(publicada.orElseThrow().getTitulo()).isEqualTo("Relatório pronto");
    }

    @Test
    void shouldNaoPublicarQuandoODestinatarioDesligouOTipo() {
        when(consultarPreferenciasDeNotificacaoUseCase.execute("user-a"))
                .thenReturn(PreferenciasDeNotificacao.de(
                        List.of(new PreferenciaNotificacao("user-a", TipoNotificacao.POSITIVA, false))));

        Optional<Notificacao> publicada = publicarNotificacaoUseCase.execute(
                "user-a", TipoNotificacao.POSITIVA, "Relatório pronto", "Mensagem");

        assertThat(publicada).isEmpty();
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void shouldNaoPublicarSemDestinatario() {
        Optional<Notificacao> publicada =
                publicarNotificacaoUseCase.execute(null, TipoNotificacao.NEGATIVA, "Título", "Mensagem");

        assertThat(publicada).isEmpty();
        verify(notificacaoRepository, never()).save(any());
    }
}
