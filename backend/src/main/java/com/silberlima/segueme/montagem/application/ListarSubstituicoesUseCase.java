package com.silberlima.segueme.montagem.application;

import com.silberlima.segueme.montagem.domain.Substituicao;
import com.silberlima.segueme.montagem.domain.SubstituicaoRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta o histórico de substituições de um Encontro, opcionalmente filtrado por equipe (RF-033). */
@Service
public class ListarSubstituicoesUseCase {

    private final SubstituicaoRepository substituicaoRepository;

    public ListarSubstituicoesUseCase(SubstituicaoRepository substituicaoRepository) {
        this.substituicaoRepository = substituicaoRepository;
    }

    @Transactional(readOnly = true)
    public Page<Substituicao> execute(UUID encontroId, UUID equipeId, Pageable pageable) {
        if (equipeId != null) {
            return substituicaoRepository.findByEncontro_IdAndEquipe_IdOrderByDataSubstituicaoDesc(
                    encontroId, equipeId, pageable);
        }
        return substituicaoRepository.findByEncontro_IdOrderByDataSubstituicaoDesc(encontroId, pageable);
    }
}
