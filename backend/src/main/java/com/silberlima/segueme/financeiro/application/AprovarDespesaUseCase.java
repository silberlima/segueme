package com.silberlima.segueme.financeiro.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiro;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiroRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aprova uma despesa pendente (RN-FINANCEIRO-02). Restrito a {@code EQUIPE_FINANCAS}/{@code PAROQUIA}
 * no controller — pendência #12 de {@code pendencias-gerais.md}.
 */
@Service
public class AprovarDespesaUseCase {

    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;

    public AprovarDespesaUseCase(LancamentoFinanceiroRepository lancamentoFinanceiroRepository) {
        this.lancamentoFinanceiroRepository = lancamentoFinanceiroRepository;
    }

    @Transactional
    public LancamentoFinanceiro execute(UUID lancamentoId, String aprovadoPor) {
        LancamentoFinanceiro lancamento = lancamentoFinanceiroRepository
                .findByIdAndDeletedFalse(lancamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento não encontrado: " + lancamentoId));
        lancamento.aprovar(aprovadoPor);
        return lancamento;
    }
}
