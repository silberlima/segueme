package com.silberlima.segueme.movimentacao.application;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.montagem.application.AdicionarIntegranteUseCase;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoCandidato;
import com.silberlima.segueme.movimentacao.domain.EmprestimoCandidatoRepository;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import com.silberlima.segueme.movimentacao.domain.StatusCandidatoEmprestimo;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Montagem efetiva um Empréstimo com sugestões enviadas pelo Setorial (RF-045, RN-18):
 * adiciona todos os candidatos ainda {@code SUGERIDO} ao roster da equipe do Encontro, via
 * {@link AdicionarIntegranteUseCase#execute(UUID, UUID, UUID, PapelIntegrante, boolean)} com
 * {@code viaEmprestimoAprovado=true}. Correspondente ao {@code EffectivateLoanUseCase} citado em
 * {@code plano-implementacao.md}.
 *
 * <p>Não há, no protótipo, uma seleção parcial de candidatos nesta etapa ("Adicionar às equipes"
 * é um único clique) — todos os candidatos sugeridos e ainda não efetivados são adicionados numa
 * única transação.
 */
@Service
public class EfetivarEmprestimoUseCase {

    private final EmprestimoRepository emprestimoRepository;
    private final EmprestimoCandidatoRepository emprestimoCandidatoRepository;
    private final AdicionarIntegranteUseCase adicionarIntegranteUseCase;

    public EfetivarEmprestimoUseCase(
            EmprestimoRepository emprestimoRepository,
            EmprestimoCandidatoRepository emprestimoCandidatoRepository,
            AdicionarIntegranteUseCase adicionarIntegranteUseCase) {
        this.emprestimoRepository = emprestimoRepository;
        this.emprestimoCandidatoRepository = emprestimoCandidatoRepository;
        this.adicionarIntegranteUseCase = adicionarIntegranteUseCase;
    }

    @Transactional
    public Emprestimo execute(UUID emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository
                .findByIdAndDeletedFalse(emprestimoId)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado: " + emprestimoId));

        List<EmprestimoCandidato> sugeridos =
                emprestimoCandidatoRepository.findByEmprestimo_IdAndStatus(emprestimoId, StatusCandidatoEmprestimo.SUGERIDO);
        if (sugeridos.isEmpty()) {
            throw new BusinessRuleViolationException("Não há candidatos sugeridos para efetivar neste empréstimo.");
        }

        for (EmprestimoCandidato candidato : sugeridos) {
            IntegranteEquipe integrante = adicionarIntegranteUseCase.execute(
                    emprestimo.getEncontro().getId(),
                    emprestimo.getEquipe().getId(),
                    candidato.getPessoa().getId(),
                    PapelIntegrante.INTEGRANTE,
                    true);
            candidato.efetivar(integrante);
        }

        emprestimo.marcarEfetivado();
        return emprestimo;
    }
}
