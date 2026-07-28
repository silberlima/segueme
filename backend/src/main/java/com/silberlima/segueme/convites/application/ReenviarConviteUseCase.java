package com.silberlima.segueme.convites.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reenvia um convite ainda pendente (RF-039), usado pelo Painel do Coordenador. */
@Service
public class ReenviarConviteUseCase {

    private final IntegranteEquipeRepository integranteEquipeRepository;

    public ReenviarConviteUseCase(IntegranteEquipeRepository integranteEquipeRepository) {
        this.integranteEquipeRepository = integranteEquipeRepository;
    }

    @Transactional
    public IntegranteEquipe execute(UUID conviteId) {
        IntegranteEquipe convite = integranteEquipeRepository
                .findById(conviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado: " + conviteId));
        convite.reenviarConvite();
        return convite;
    }
}
