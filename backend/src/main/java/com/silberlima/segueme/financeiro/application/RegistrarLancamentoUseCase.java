package com.silberlima.segueme.financeiro.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiro;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiroRepository;
import com.silberlima.segueme.financeiro.domain.TipoLancamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registra um lançamento (receita ou despesa) de um Encontro (RF-047). */
@Service
public class RegistrarLancamentoUseCase {

    private final EncontroRepository encontroRepository;
    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;

    public RegistrarLancamentoUseCase(
            EncontroRepository encontroRepository, LancamentoFinanceiroRepository lancamentoFinanceiroRepository) {
        this.encontroRepository = encontroRepository;
        this.lancamentoFinanceiroRepository = lancamentoFinanceiroRepository;
    }

    @Transactional
    public LancamentoFinanceiro execute(UUID encontroId, TipoLancamento tipo, String categoria, BigDecimal valor, LocalDate data) {
        Encontro encontro = encontroRepository
                .findByIdAndDeletedFalse(encontroId)
                .orElseThrow(() -> new ResourceNotFoundException("Encontro não encontrado: " + encontroId));

        LancamentoFinanceiro lancamento = tipo == TipoLancamento.RECEITA
                ? LancamentoFinanceiro.receita(encontro, categoria, valor, data)
                : LancamentoFinanceiro.despesa(encontro, categoria, valor, data);

        return lancamentoFinanceiroRepository.save(lancamento);
    }
}
