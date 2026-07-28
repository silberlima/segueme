package com.silberlima.segueme.movimentacao.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.EquipeRepository;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import com.silberlima.segueme.pessoas.domain.Competencia;
import com.silberlima.segueme.pessoas.domain.CompetenciaRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Solicitação da Equipe Montagem por um Seguidor de outra paróquia para uma vaga de equipe do
 * Encontro corrente (RF-042, RN-MOVIMENTO-02/04). Correspondente ao {@code RequestLoanUseCase}
 * citado em {@code plano-implementacao.md}.
 */
@Service
public class SolicitarEmprestimoUseCase {

    private final EncontroRepository encontroRepository;
    private final EquipeRepository equipeRepository;
    private final CompetenciaRepository competenciaRepository;
    private final EmprestimoRepository emprestimoRepository;

    public SolicitarEmprestimoUseCase(
            EncontroRepository encontroRepository,
            EquipeRepository equipeRepository,
            CompetenciaRepository competenciaRepository,
            EmprestimoRepository emprestimoRepository) {
        this.encontroRepository = encontroRepository;
        this.equipeRepository = equipeRepository;
        this.competenciaRepository = competenciaRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    @Transactional
    public Emprestimo execute(UUID encontroId, UUID equipeId, UUID competenciaRequeridaId) {
        Encontro encontro = encontroRepository
                .findByIdAndDeletedFalse(encontroId)
                .orElseThrow(() -> new ResourceNotFoundException("Encontro não encontrado: " + encontroId));
        Equipe equipe = equipeRepository
                .findByIdAndDeletedFalse(equipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe não encontrada: " + equipeId));
        Competencia competenciaRequerida = null;
        if (competenciaRequeridaId != null) {
            competenciaRequerida = competenciaRepository
                    .findByIdAndDeletedFalse(competenciaRequeridaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Competência não encontrada: " + competenciaRequeridaId));
        }

        return emprestimoRepository.save(new Emprestimo(encontro, equipe, competenciaRequerida));
    }
}
