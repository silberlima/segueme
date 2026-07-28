package com.silberlima.segueme.montagem.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.montagem.application.dto.EquipeComposicao;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.EquipeRepository;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta o detalhe de uma equipe de serviço de um Encontro: composição, vagas e pré-requisito (RF-028). */
@Service
public class ConsultarEquipeUseCase {

    private final EquipeRepository equipeRepository;
    private final IntegranteEquipeRepository integranteEquipeRepository;

    public ConsultarEquipeUseCase(EquipeRepository equipeRepository, IntegranteEquipeRepository integranteEquipeRepository) {
        this.equipeRepository = equipeRepository;
        this.integranteEquipeRepository = integranteEquipeRepository;
    }

    @Transactional(readOnly = true)
    public EquipeComposicao execute(UUID encontroId, UUID equipeId) {
        Equipe equipe = equipeRepository
                .findByIdAndDeletedFalse(equipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe não encontrada: " + equipeId));
        List<IntegranteEquipe> integrantes = integranteEquipeRepository.findByEncontro_IdAndEquipe_Id(encontroId, equipeId);
        return new EquipeComposicao(equipe, integrantes);
    }
}
