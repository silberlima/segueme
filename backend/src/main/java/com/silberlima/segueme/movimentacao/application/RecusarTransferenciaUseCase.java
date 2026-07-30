package com.silberlima.segueme.movimentacao.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import com.silberlima.segueme.movimentacao.domain.TransferenciaDecididaEvent;
import com.silberlima.segueme.movimentacao.domain.TransferenciaRepository;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Recusa uma Transferência pendente (RF-041, restrito ao Setorial): a pessoa permanece na paróquia de origem. */
@Service
public class RecusarTransferenciaUseCase {

    private final TransferenciaRepository transferenciaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RecusarTransferenciaUseCase(
            TransferenciaRepository transferenciaRepository, ApplicationEventPublisher eventPublisher) {
        this.transferenciaRepository = transferenciaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Transferencia execute(UUID transferenciaId, String justificativa) {
        Transferencia transferencia = transferenciaRepository
                .findByIdAndDeletedFalse(transferenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Transferência não encontrada: " + transferenciaId));

        transferencia.recusar(justificativa);

        eventPublisher.publishEvent(new TransferenciaDecididaEvent(
                transferencia.getPessoa().getNomeCompleto(),
                transferencia.getParoquiaOrigem().getNome(),
                transferencia.getParoquiaDestino().getNome(),
                false,
                justificativa,
                transferencia.getCreatedBy()));

        return transferencia;
    }
}
