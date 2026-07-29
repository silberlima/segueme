package com.silberlima.segueme.financeiro.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.financeiro.application.dto.ResumoFinanceiro;
import com.silberlima.segueme.financeiro.domain.CampanhaRepository;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiroRepository;
import com.silberlima.segueme.financeiro.domain.StatusCampanha;
import com.silberlima.segueme.financeiro.domain.TipoLancamento;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consolida saldo, totais e campanhas ativas de um Encontro (RN-FINANCEIRO-04): calculado, não persistido. */
@Service
public class ObterResumoFinanceiroUseCase {

    private final EncontroRepository encontroRepository;
    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;
    private final CampanhaRepository campanhaRepository;

    public ObterResumoFinanceiroUseCase(
            EncontroRepository encontroRepository,
            LancamentoFinanceiroRepository lancamentoFinanceiroRepository,
            CampanhaRepository campanhaRepository) {
        this.encontroRepository = encontroRepository;
        this.lancamentoFinanceiroRepository = lancamentoFinanceiroRepository;
        this.campanhaRepository = campanhaRepository;
    }

    @Transactional(readOnly = true)
    public ResumoFinanceiro execute(UUID encontroId) {
        encontroRepository
                .findByIdAndDeletedFalse(encontroId)
                .orElseThrow(() -> new ResourceNotFoundException("Encontro não encontrado: " + encontroId));

        BigDecimal totalReceitas = lancamentoFinanceiroRepository.somarPorTipo(encontroId, TipoLancamento.RECEITA);
        BigDecimal totalDespesas = lancamentoFinanceiroRepository.somarPorTipo(encontroId, TipoLancamento.DESPESA);
        long campanhasAtivas = campanhaRepository.countByEncontro_IdAndStatusAndDeletedFalse(
                encontroId, StatusCampanha.ATIVA);

        return new ResumoFinanceiro(totalReceitas.subtract(totalDespesas), totalReceitas, totalDespesas, campanhasAtivas);
    }
}
