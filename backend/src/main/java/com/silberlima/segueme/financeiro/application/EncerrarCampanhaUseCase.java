package com.silberlima.segueme.financeiro.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.financeiro.domain.Campanha;
import com.silberlima.segueme.financeiro.domain.CampanhaRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Encerra uma campanha ativa, preservando o resultado final (RN-FINANCEIRO-03). */
@Service
public class EncerrarCampanhaUseCase {

    private final CampanhaRepository campanhaRepository;

    public EncerrarCampanhaUseCase(CampanhaRepository campanhaRepository) {
        this.campanhaRepository = campanhaRepository;
    }

    @Transactional
    public Campanha execute(UUID campanhaId) {
        Campanha campanha = campanhaRepository
                .findByIdAndDeletedFalse(campanhaId)
                .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada: " + campanhaId));
        campanha.encerrar();
        return campanha;
    }
}
