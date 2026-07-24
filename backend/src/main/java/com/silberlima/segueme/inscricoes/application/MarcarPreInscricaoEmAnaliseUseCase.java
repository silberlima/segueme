package com.silberlima.segueme.inscricoes.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Marca uma Pré-inscrição como "em análise" enquanto a Equipe Fichas a revisa. */
@Service
public class MarcarPreInscricaoEmAnaliseUseCase {

    private final PreInscricaoRepository preInscricaoRepository;

    public MarcarPreInscricaoEmAnaliseUseCase(PreInscricaoRepository preInscricaoRepository) {
        this.preInscricaoRepository = preInscricaoRepository;
    }

    @Transactional
    public PreInscricao execute(UUID id) {
        PreInscricao preInscricao = preInscricaoRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pré-inscrição não encontrada: " + id));
        preInscricao.marcarEmAnalise();
        return preInscricao;
    }
}
