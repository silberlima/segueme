package com.silberlima.segueme.financeiro.application;

import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiro;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiroRepository;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiroSpecification;
import com.silberlima.segueme.financeiro.domain.SituacaoDespesa;
import com.silberlima.segueme.financeiro.domain.TipoLancamento;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lista lançamentos financeiros do escopo territorial do usuário, com filtros opcionais (RF-048/049). */
@Service
public class ListarLancamentosUseCase {

    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;

    public ListarLancamentosUseCase(LancamentoFinanceiroRepository lancamentoFinanceiroRepository) {
        this.lancamentoFinanceiroRepository = lancamentoFinanceiroRepository;
    }

    @Transactional(readOnly = true)
    public Page<LancamentoFinanceiro> execute(
            EscopoTerritorial escopo, UUID encontroId, TipoLancamento tipo, SituacaoDespesa situacao, Pageable pageable) {
        Specification<LancamentoFinanceiro> spec = Specification.allOf(
                LancamentoFinanceiroSpecification.naoDeletado(),
                LancamentoFinanceiroSpecification.comEscopo(escopo),
                LancamentoFinanceiroSpecification.comEncontro(encontroId),
                LancamentoFinanceiroSpecification.comTipo(tipo),
                LancamentoFinanceiroSpecification.comSituacao(situacao));
        return lancamentoFinanceiroRepository.findAll(spec, pageable);
    }
}
