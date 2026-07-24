package com.silberlima.segueme.inscricoes.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta uma Pré-inscrição por id, usada pela tela de Ficha de Inscrição para carregar o candidato. */
@Service
public class ConsultarPreInscricaoUseCase {

    private final PreInscricaoRepository preInscricaoRepository;

    public ConsultarPreInscricaoUseCase(PreInscricaoRepository preInscricaoRepository) {
        this.preInscricaoRepository = preInscricaoRepository;
    }

    @Transactional(readOnly = true)
    public PreInscricao execute(UUID id) {
        return preInscricaoRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pré-inscrição não encontrada: " + id));
    }
}
