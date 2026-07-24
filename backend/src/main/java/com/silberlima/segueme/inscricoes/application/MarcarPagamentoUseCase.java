package com.silberlima.segueme.inscricoes.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.inscricoes.domain.FichaInscricao;
import com.silberlima.segueme.inscricoes.domain.FichaInscricaoRepository;
import com.silberlima.segueme.inscricoes.domain.StatusPagamento;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Alterna o status de pagamento de uma Ficha de Inscrição confirmada. Placeholder mínimo até a
 * Fase 9 (Financeiro) definir valores, formas e prazos de pagamento (pendência #13 parcial).
 */
@Service
public class MarcarPagamentoUseCase {

    private final FichaInscricaoRepository fichaInscricaoRepository;

    public MarcarPagamentoUseCase(FichaInscricaoRepository fichaInscricaoRepository) {
        this.fichaInscricaoRepository = fichaInscricaoRepository;
    }

    @Transactional
    public FichaInscricao execute(UUID fichaInscricaoId, StatusPagamento statusPagamento) {
        FichaInscricao ficha = fichaInscricaoRepository
                .findByIdAndDeletedFalse(fichaInscricaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha de inscrição não encontrada: " + fichaInscricaoId));
        if (statusPagamento == StatusPagamento.PAGO) {
            ficha.marcarComoPago();
        } else {
            ficha.marcarComoPendente();
        }
        return ficha;
    }
}
