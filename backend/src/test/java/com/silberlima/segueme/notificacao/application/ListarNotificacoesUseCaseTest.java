package com.silberlima.segueme.notificacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.notificacao.domain.Notificacao;
import com.silberlima.segueme.notificacao.domain.NotificacaoRepository;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ListarNotificacoesUseCaseTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Test
    void shouldDelegateToRepositoryScopedByDestinatario() {
        String destinatarioId = "user-1";
        Pageable pageable = PageRequest.of(0, 10);
        Notificacao notificacao = new Notificacao(destinatarioId, TipoNotificacao.INFORMATIVA, "Título", "Mensagem");
        when(notificacaoRepository.findByDestinatarioIdAndDeletedFalse(destinatarioId, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(notificacao)));

        ListarNotificacoesUseCase useCase = new ListarNotificacoesUseCase(notificacaoRepository);
        var resultado = useCase.execute(destinatarioId, pageable);

        assertThat(resultado.getContent()).containsExactly(notificacao);
        verify(notificacaoRepository).findByDestinatarioIdAndDeletedFalse(destinatarioId, pageable);
    }
}
