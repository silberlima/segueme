package com.silberlima.segueme.financeiro.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.financeiro.domain.Campanha;
import com.silberlima.segueme.financeiro.domain.CampanhaRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cria uma campanha de arrecadação de um Encontro (RF-050). */
@Service
public class CriarCampanhaUseCase {

    private final EncontroRepository encontroRepository;
    private final CampanhaRepository campanhaRepository;

    public CriarCampanhaUseCase(EncontroRepository encontroRepository, CampanhaRepository campanhaRepository) {
        this.encontroRepository = encontroRepository;
        this.campanhaRepository = campanhaRepository;
    }

    @Transactional
    public Campanha execute(UUID encontroId, String nome, BigDecimal meta) {
        Encontro encontro = encontroRepository
                .findByIdAndDeletedFalse(encontroId)
                .orElseThrow(() -> new ResourceNotFoundException("Encontro não encontrado: " + encontroId));
        return campanhaRepository.save(new Campanha(encontro, nome, meta));
    }
}
