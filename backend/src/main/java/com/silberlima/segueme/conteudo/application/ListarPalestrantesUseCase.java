package com.silberlima.segueme.conteudo.application;

import com.silberlima.segueme.conteudo.domain.Palestrante;
import com.silberlima.segueme.conteudo.domain.PalestranteRepository;
import com.silberlima.segueme.conteudo.domain.PalestranteSpecification;
import com.silberlima.segueme.conteudo.domain.TipoPalestrante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lista o cadastro de palestrantes, com filtro opcional por tipo (RF-052, `screen_palestrantes`). */
@Service
public class ListarPalestrantesUseCase {

    private final PalestranteRepository palestranteRepository;

    public ListarPalestrantesUseCase(PalestranteRepository palestranteRepository) {
        this.palestranteRepository = palestranteRepository;
    }

    @Transactional(readOnly = true)
    public Page<Palestrante> execute(TipoPalestrante tipo, Pageable pageable) {
        Specification<Palestrante> spec = Specification.allOf(
                PalestranteSpecification.naoDeletado(), PalestranteSpecification.comTipo(tipo));
        return palestranteRepository.findAll(spec, pageable);
    }
}
