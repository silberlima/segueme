package com.silberlima.segueme.financeiro.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiro;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiroRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Marca uma despesa aprovada como paga (RN-FINANCEIRO-02). */
@Service
public class PagarDespesaUseCase {

    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;

    public PagarDespesaUseCase(LancamentoFinanceiroRepository lancamentoFinanceiroRepository) {
        this.lancamentoFinanceiroRepository = lancamentoFinanceiroRepository;
    }

    @Transactional
    public LancamentoFinanceiro execute(UUID lancamentoId) {
        LancamentoFinanceiro lancamento = lancamentoFinanceiroRepository
                .findByIdAndDeletedFalse(lancamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento não encontrado: " + lancamentoId));
        lancamento.pagar();
        return lancamento;
    }
}
