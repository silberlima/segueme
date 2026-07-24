package com.silberlima.segueme.inscricoes.application;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoRepository;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoSpecification;
import com.silberlima.segueme.inscricoes.domain.StatusPreInscricao;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lista Pré-inscrições do escopo territorial do usuário, com busca por encontro/status/nome (`inscricoes.md` 2.1). */
@Service
public class ListarPreInscricoesUseCase {

    private final PreInscricaoRepository preInscricaoRepository;

    public ListarPreInscricoesUseCase(PreInscricaoRepository preInscricaoRepository) {
        this.preInscricaoRepository = preInscricaoRepository;
    }

    @Transactional(readOnly = true)
    public Page<PreInscricao> execute(
            EscopoTerritorial escopo, UUID encontroId, StatusPreInscricao status, String nome, Pageable pageable) {
        Specification<PreInscricao> spec = Specification.allOf(
                PreInscricaoSpecification.naoDeletada(),
                PreInscricaoSpecification.comEscopo(escopo),
                PreInscricaoSpecification.comEncontro(encontroId),
                PreInscricaoSpecification.comStatus(status),
                PreInscricaoSpecification.comNomeCandidato(nome),
                PreInscricaoSpecification.comPessoaCarregada());
        return preInscricaoRepository.findAll(spec, pageable);
    }
}
