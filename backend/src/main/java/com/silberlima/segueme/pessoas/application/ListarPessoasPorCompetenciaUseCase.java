package com.silberlima.segueme.pessoas.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.pessoas.domain.CompetenciaRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** Lista as Pessoas que possuem uma dada competência (RF-013, ação "ver pessoas por competência"). */
@Service
public class ListarPessoasPorCompetenciaUseCase {

    private final CompetenciaRepository competenciaRepository;
    private final ListarPessoasUseCase listarPessoasUseCase;

    public ListarPessoasPorCompetenciaUseCase(
            CompetenciaRepository competenciaRepository, ListarPessoasUseCase listarPessoasUseCase) {
        this.competenciaRepository = competenciaRepository;
        this.listarPessoasUseCase = listarPessoasUseCase;
    }

    public Page<Pessoa> execute(EscopoTerritorial escopo, UUID competenciaId, Pageable pageable) {
        if (competenciaRepository.findByIdAndDeletedFalse(competenciaId).isEmpty()) {
            throw new ResourceNotFoundException("Competência não encontrada: " + competenciaId);
        }
        return listarPessoasUseCase.execute(escopo, null, null, competenciaId, pageable);
    }
}
