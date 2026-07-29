package com.silberlima.segueme.financeiro.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.financeiro.domain.Campanha;
import com.silberlima.segueme.financeiro.domain.CampanhaRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registra um valor arrecadado numa campanha ativa (RF-050, RN-FINANCEIRO-03). */
@Service
public class RegistrarArrecadacaoUseCase {

    private final CampanhaRepository campanhaRepository;

    public RegistrarArrecadacaoUseCase(CampanhaRepository campanhaRepository) {
        this.campanhaRepository = campanhaRepository;
    }

    @Transactional
    public Campanha execute(UUID campanhaId, BigDecimal valor) {
        Campanha campanha = campanhaRepository
                .findByIdAndDeletedFalse(campanhaId)
                .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada: " + campanhaId));
        campanha.registrarArrecadacao(valor);
        return campanha;
    }
}
