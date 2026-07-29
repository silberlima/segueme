package com.silberlima.segueme.conteudo.application;

import com.silberlima.segueme.conteudo.domain.DesignacaoPalestra;
import com.silberlima.segueme.conteudo.domain.DesignacaoPalestraRepository;
import com.silberlima.segueme.conteudo.domain.Palestra;
import com.silberlima.segueme.conteudo.domain.PalestraRepository;
import com.silberlima.segueme.conteudo.domain.Palestrante;
import com.silberlima.segueme.conteudo.domain.PalestranteRepository;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Designa (ou substitui) o palestrante de um tema num Encontro (RF-052, UC — Designar
 * palestrante). Quem tem permissão para editar essa designação não é especificado no protótipo
 * (pendência #31 de {@code pendencias-gerais.md}) — restrito a Paróquia e Equipe Comando no
 * controller até validação do PO.
 */
@Service
public class DesignarPalestranteUseCase {

    private final EncontroRepository encontroRepository;
    private final PalestraRepository palestraRepository;
    private final PalestranteRepository palestranteRepository;
    private final DesignacaoPalestraRepository designacaoPalestraRepository;

    public DesignarPalestranteUseCase(
            EncontroRepository encontroRepository,
            PalestraRepository palestraRepository,
            PalestranteRepository palestranteRepository,
            DesignacaoPalestraRepository designacaoPalestraRepository) {
        this.encontroRepository = encontroRepository;
        this.palestraRepository = palestraRepository;
        this.palestranteRepository = palestranteRepository;
        this.designacaoPalestraRepository = designacaoPalestraRepository;
    }

    @Transactional
    public DesignacaoPalestra execute(UUID encontroId, UUID palestraId, UUID palestranteId) {
        Encontro encontro = encontroRepository
                .findByIdAndDeletedFalse(encontroId)
                .orElseThrow(() -> new ResourceNotFoundException("Encontro não encontrado: " + encontroId));
        Palestra palestra = palestraRepository
                .findById(palestraId)
                .orElseThrow(() -> new ResourceNotFoundException("Palestra não encontrada: " + palestraId));
        Palestrante palestrante = palestranteRepository
                .findByIdAndDeletedFalse(palestranteId)
                .orElseThrow(() -> new ResourceNotFoundException("Palestrante não encontrado: " + palestranteId));

        DesignacaoPalestra designacao = designacaoPalestraRepository
                .findByEncontro_IdAndPalestra_IdAndDeletedFalse(encontroId, palestraId)
                .orElseGet(() -> new DesignacaoPalestra(encontro, palestra));
        designacao.designar(palestrante);

        return designacaoPalestraRepository.save(designacao);
    }
}
