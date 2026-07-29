package com.silberlima.segueme.conteudo.application;

import com.silberlima.segueme.conteudo.application.dto.PalestraDoEncontro;
import com.silberlima.segueme.conteudo.domain.DesignacaoPalestra;
import com.silberlima.segueme.conteudo.domain.DesignacaoPalestraRepository;
import com.silberlima.segueme.conteudo.domain.Palestra;
import com.silberlima.segueme.conteudo.domain.PalestraRepository;
import com.silberlima.segueme.conteudo.domain.Palestrante;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista os 7 temas oficiais em ordem fixa, com o palestrante designado em cada um para um
 * Encontro específico (RF-051, RN-CONTEUDO-01). Um tema sem designação ainda registrada aparece
 * com {@code palestrante() == null}.
 */
@Service
public class ListarPalestrasDoEncontroUseCase {

    private final PalestraRepository palestraRepository;
    private final DesignacaoPalestraRepository designacaoPalestraRepository;

    public ListarPalestrasDoEncontroUseCase(
            PalestraRepository palestraRepository, DesignacaoPalestraRepository designacaoPalestraRepository) {
        this.palestraRepository = palestraRepository;
        this.designacaoPalestraRepository = designacaoPalestraRepository;
    }

    @Transactional(readOnly = true)
    public List<PalestraDoEncontro> execute(UUID encontroId) {
        List<Palestra> palestras = palestraRepository.findByDeletedFalseOrderByNumeroAsc();

        Map<UUID, Palestrante> palestrantePorPalestra = designacaoPalestraRepository
                .findByEncontro_IdAndDeletedFalse(encontroId)
                .stream()
                .filter(designacao -> designacao.getPalestrante() != null)
                .collect(Collectors.toMap(
                        designacao -> designacao.getPalestra().getId(), DesignacaoPalestra::getPalestrante));

        return palestras.stream()
                .map(palestra -> new PalestraDoEncontro(palestra, palestrantePorPalestra.get(palestra.getId())))
                .toList();
    }
}
