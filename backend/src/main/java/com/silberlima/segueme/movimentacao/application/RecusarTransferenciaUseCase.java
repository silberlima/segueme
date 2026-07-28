package com.silberlima.segueme.movimentacao.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import com.silberlima.segueme.movimentacao.domain.TransferenciaRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Recusa uma Transferência pendente (RF-041, restrito ao Setorial): a pessoa permanece na paróquia de origem. */
@Service
public class RecusarTransferenciaUseCase {

    private final TransferenciaRepository transferenciaRepository;

    public RecusarTransferenciaUseCase(TransferenciaRepository transferenciaRepository) {
        this.transferenciaRepository = transferenciaRepository;
    }

    @Transactional
    public Transferencia execute(UUID transferenciaId, String justificativa) {
        Transferencia transferencia = transferenciaRepository
                .findByIdAndDeletedFalse(transferenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Transferência não encontrada: " + transferenciaId));

        transferencia.recusar(justificativa);

        return transferencia;
    }
}
