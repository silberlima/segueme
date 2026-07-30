package com.silberlima.segueme.movimentacao.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoDecididoEvent;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Setorial recusa uma solicitação de Empréstimo antes de selecionar candidatos (RF-046, RN-17). */
@Service
public class RecusarEmprestimoUseCase {

    private final EmprestimoRepository emprestimoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RecusarEmprestimoUseCase(
            EmprestimoRepository emprestimoRepository, ApplicationEventPublisher eventPublisher) {
        this.emprestimoRepository = emprestimoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Emprestimo execute(UUID emprestimoId, String justificativa) {
        Emprestimo emprestimo = emprestimoRepository
                .findByIdAndDeletedFalse(emprestimoId)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado: " + emprestimoId));

        emprestimo.recusar(justificativa);

        eventPublisher.publishEvent(new EmprestimoDecididoEvent(
                emprestimo.getEquipe().getNome(),
                emprestimo.getEncontro().getNumero(),
                0,
                true,
                justificativa,
                emprestimo.getCreatedBy()));

        return emprestimo;
    }
}
