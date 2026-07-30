package com.silberlima.segueme.financeiro.application;

import com.silberlima.segueme.financeiro.domain.Campanha;
import com.silberlima.segueme.financeiro.domain.CampanhaRepository;
import com.silberlima.segueme.financeiro.domain.CampanhaSpecification;
import com.silberlima.segueme.financeiro.domain.StatusCampanha;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lista campanhas de arrecadação do escopo territorial do usuário, com filtros opcionais (RF-050). */
@Service
public class ListarCampanhasUseCase {

    private final CampanhaRepository campanhaRepository;

    public ListarCampanhasUseCase(CampanhaRepository campanhaRepository) {
        this.campanhaRepository = campanhaRepository;
    }

    @Transactional(readOnly = true)
    public Page<Campanha> execute(EscopoTerritorial escopo, UUID encontroId, StatusCampanha status, Pageable pageable) {
        Specification<Campanha> spec = Specification.allOf(
                CampanhaSpecification.naoDeletada(),
                CampanhaSpecification.comEscopo(escopo),
                CampanhaSpecification.comEncontro(encontroId),
                CampanhaSpecification.comStatus(status),
                CampanhaSpecification.comEncontroCarregado());
        return campanhaRepository.findAll(spec, pageable);
    }
}
