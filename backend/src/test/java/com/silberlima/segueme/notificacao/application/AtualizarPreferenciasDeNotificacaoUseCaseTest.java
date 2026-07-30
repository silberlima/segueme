package com.silberlima.segueme.notificacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.notificacao.domain.PreferenciaNotificacao;
import com.silberlima.segueme.notificacao.domain.PreferenciaNotificacaoRepository;
import com.silberlima.segueme.notificacao.domain.PreferenciasDeNotificacao;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Cobre a gravação dos toggles de Configurações: cria o que não existe, atualiza o que existe. */
@ExtendWith(MockitoExtension.class)
class AtualizarPreferenciasDeNotificacaoUseCaseTest {

    @Mock
    private PreferenciaNotificacaoRepository preferenciaNotificacaoRepository;

    @InjectMocks
    private AtualizarPreferenciasDeNotificacaoUseCase atualizarPreferenciasDeNotificacaoUseCase;

    @Test
    void shouldCriarPreferenciaQuandoUsuarioNuncaEscolheu() {
        when(preferenciaNotificacaoRepository.findByUsuarioIdAndTipoAndDeletedFalse(
                        "user-a", TipoNotificacao.NEGATIVA))
                .thenReturn(Optional.empty());
        when(preferenciaNotificacaoRepository.findByUsuarioIdAndDeletedFalse("user-a")).thenReturn(List.of());

        atualizarPreferenciasDeNotificacaoUseCase.execute("user-a", Map.of(TipoNotificacao.NEGATIVA, false));

        ArgumentCaptor<PreferenciaNotificacao> gravada = ArgumentCaptor.forClass(PreferenciaNotificacao.class);
        verify(preferenciaNotificacaoRepository).save(gravada.capture());
        assertThat(gravada.getValue().getUsuarioId()).isEqualTo("user-a");
        assertThat(gravada.getValue().getTipo()).isEqualTo(TipoNotificacao.NEGATIVA);
        assertThat(gravada.getValue().isAtivo()).isFalse();
    }

    @Test
    void shouldAtualizarPreferenciaJaExistente() {
        PreferenciaNotificacao existente = new PreferenciaNotificacao("user-a", TipoNotificacao.POSITIVA, false);
        when(preferenciaNotificacaoRepository.findByUsuarioIdAndTipoAndDeletedFalse(
                        "user-a", TipoNotificacao.POSITIVA))
                .thenReturn(Optional.of(existente));
        when(preferenciaNotificacaoRepository.findByUsuarioIdAndDeletedFalse("user-a"))
                .thenReturn(List.of(existente));

        atualizarPreferenciasDeNotificacaoUseCase.execute("user-a", Map.of(TipoNotificacao.POSITIVA, true));

        assertThat(existente.isAtivo()).isTrue();
        verify(preferenciaNotificacaoRepository).save(existente);
    }

    @Test
    void shouldDevolverAsPreferenciasCompletasAposGravar() {
        when(preferenciaNotificacaoRepository.findByUsuarioIdAndTipoAndDeletedFalse(any(), any()))
                .thenReturn(Optional.empty());
        when(preferenciaNotificacaoRepository.findByUsuarioIdAndDeletedFalse("user-a"))
                .thenReturn(List.of(new PreferenciaNotificacao("user-a", TipoNotificacao.INFORMATIVA, false)));

        PreferenciasDeNotificacao preferencias = atualizarPreferenciasDeNotificacaoUseCase.execute(
                "user-a", Map.of(TipoNotificacao.INFORMATIVA, false));

        assertThat(preferencias.ativa(TipoNotificacao.INFORMATIVA)).isFalse();
        assertThat(preferencias.ativa(TipoNotificacao.POSITIVA)).isTrue();
    }
}
