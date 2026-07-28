package com.silberlima.segueme.movimentacao.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import com.silberlima.segueme.movimentacao.domain.TransferenciaRepository;
import com.silberlima.segueme.pessoas.domain.HistoricoEvento;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.TipoEventoHistorico;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aprova uma Transferência pendente (RF-041, RN-MOVIMENTO-01, restrito ao Setorial via
 * {@code @PreAuthorize} no controller): efetiva a mudança de paróquia da Pessoa e registra o
 * evento no histórico (RN-MOVIMENTO-03).
 */
@Service
public class AprovarTransferenciaUseCase {

    private final TransferenciaRepository transferenciaRepository;
    private final HistoricoEventoRepository historicoEventoRepository;

    public AprovarTransferenciaUseCase(
            TransferenciaRepository transferenciaRepository, HistoricoEventoRepository historicoEventoRepository) {
        this.transferenciaRepository = transferenciaRepository;
        this.historicoEventoRepository = historicoEventoRepository;
    }

    @Transactional
    public Transferencia execute(UUID transferenciaId) {
        Transferencia transferencia = transferenciaRepository
                .findByIdAndDeletedFalse(transferenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Transferência não encontrada: " + transferenciaId));

        transferencia.aprovar();

        Pessoa pessoa = transferencia.getPessoa();
        pessoa.transferirParaParoquia(transferencia.getParoquiaDestino());

        historicoEventoRepository.save(new HistoricoEvento(
                pessoa,
                TipoEventoHistorico.TRANSFERENCIA_PAROQUIA,
                Instant.now(),
                "Transferido(a) da paróquia " + transferencia.getParoquiaOrigem().getNome() + " para "
                        + transferencia.getParoquiaDestino().getNome() + " (aprovado pelo Setorial)."));

        return transferencia;
    }
}
