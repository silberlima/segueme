package com.silberlima.segueme.convites.application;

import com.silberlima.segueme.convites.domain.ConviteRecusadoEvent;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra a recusa de um convite com justificativa obrigatória (RF-037, RN-CONVITE-02) e publica
 * {@link ConviteRecusadoEvent} para notificar imediatamente quem emitiu o convite (RF-038/RN-12 —
 * ver a limitação documentada em {@link ConviteRecusadoEvent}).
 */
@Service
public class RecusarConviteUseCase {

    private final IntegranteEquipeRepository integranteEquipeRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RecusarConviteUseCase(
            IntegranteEquipeRepository integranteEquipeRepository, ApplicationEventPublisher eventPublisher) {
        this.integranteEquipeRepository = integranteEquipeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public IntegranteEquipe execute(UUID conviteId, String justificativa) {
        IntegranteEquipe convite = integranteEquipeRepository
                .findById(conviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado: " + conviteId));
        convite.recusar(justificativa);

        eventPublisher.publishEvent(new ConviteRecusadoEvent(
                convite.getId(),
                convite.getEncontro().getId(),
                convite.getEquipe().getNome(),
                convite.getPessoa().getNomeCompleto(),
                justificativa,
                convite.getCreatedBy()));

        return convite;
    }
}
