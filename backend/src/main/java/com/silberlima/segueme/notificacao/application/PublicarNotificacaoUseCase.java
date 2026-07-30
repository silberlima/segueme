package com.silberlima.segueme.notificacao.application;

import com.silberlima.segueme.notificacao.domain.Notificacao;
import com.silberlima.segueme.notificacao.domain.NotificacaoRepository;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Ponto único por onde uma notificação entra na central (RN-SISTEMA-01). Existe para que a
 * preferência do destinatário (RF-057) seja respeitada sem que cada domínio que notifica precise
 * conhecê-la: quem desligou um tipo simplesmente não recebe notificação daquele tipo.
 */
@Service
public class PublicarNotificacaoUseCase {

    private final NotificacaoRepository notificacaoRepository;
    private final ConsultarPreferenciasDeNotificacaoUseCase consultarPreferenciasDeNotificacaoUseCase;

    public PublicarNotificacaoUseCase(
            NotificacaoRepository notificacaoRepository,
            ConsultarPreferenciasDeNotificacaoUseCase consultarPreferenciasDeNotificacaoUseCase) {
        this.notificacaoRepository = notificacaoRepository;
        this.consultarPreferenciasDeNotificacaoUseCase = consultarPreferenciasDeNotificacaoUseCase;
    }

    /** @return a notificação criada, ou vazio quando o destinatário desligou o tipo. */
    public Optional<Notificacao> execute(
            String destinatarioId, TipoNotificacao tipo, String titulo, String mensagem) {
        if (destinatarioId == null || destinatarioId.isBlank()) {
            return Optional.empty();
        }
        if (!consultarPreferenciasDeNotificacaoUseCase.execute(destinatarioId).ativa(tipo)) {
            return Optional.empty();
        }
        return Optional.of(notificacaoRepository.save(new Notificacao(destinatarioId, tipo, titulo, mensagem)));
    }
}
