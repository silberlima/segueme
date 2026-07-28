package com.silberlima.segueme.convites.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registra o aceite de um convite (RF-036, RN-CONVITE-01). */
@Service
public class AceitarConviteUseCase {

    private final IntegranteEquipeRepository integranteEquipeRepository;

    public AceitarConviteUseCase(IntegranteEquipeRepository integranteEquipeRepository) {
        this.integranteEquipeRepository = integranteEquipeRepository;
    }

    @Transactional
    public IntegranteEquipe execute(UUID conviteId) {
        IntegranteEquipe convite = integranteEquipeRepository
                .findById(conviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado: " + conviteId));
        convite.aceitar();
        return convite;
    }
}
