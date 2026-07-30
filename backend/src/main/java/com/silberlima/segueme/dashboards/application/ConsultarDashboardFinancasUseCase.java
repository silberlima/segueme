package com.silberlima.segueme.dashboards.application;

import com.silberlima.segueme.dashboards.application.dto.DashboardFinancasResult;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroCorrenteResolver;
import com.silberlima.segueme.financeiro.application.ListarCampanhasUseCase;
import com.silberlima.segueme.financeiro.application.ListarLancamentosUseCase;
import com.silberlima.segueme.financeiro.application.ObterResumoFinanceiroUseCase;
import com.silberlima.segueme.financeiro.application.dto.ResumoFinanceiro;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Painel Financeiro (`dash_financas`, RF-053, RN-DASH-01): saldo, receitas, despesas e campanhas
 * ativas do Encontro corrente, mais as campanhas em progresso e os lançamentos recentes. Os números
 * vêm dos mesmos use cases da tela de financeiro (RN-FINANCEIRO-04), nunca de um cálculo paralelo.
 */
@Service
public class ConsultarDashboardFinancasUseCase {

    private static final int MAXIMO_LISTAS = 5;
    private static final ResumoFinanceiro FINANCEIRO_VAZIO =
            new ResumoFinanceiro(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);

    private final EncontroCorrenteResolver encontroCorrenteResolver;
    private final ObterResumoFinanceiroUseCase obterResumoFinanceiroUseCase;
    private final ListarCampanhasUseCase listarCampanhasUseCase;
    private final ListarLancamentosUseCase listarLancamentosUseCase;

    public ConsultarDashboardFinancasUseCase(
            EncontroCorrenteResolver encontroCorrenteResolver,
            ObterResumoFinanceiroUseCase obterResumoFinanceiroUseCase,
            ListarCampanhasUseCase listarCampanhasUseCase,
            ListarLancamentosUseCase listarLancamentosUseCase) {
        this.encontroCorrenteResolver = encontroCorrenteResolver;
        this.obterResumoFinanceiroUseCase = obterResumoFinanceiroUseCase;
        this.listarCampanhasUseCase = listarCampanhasUseCase;
        this.listarLancamentosUseCase = listarLancamentosUseCase;
    }

    @Transactional(readOnly = true)
    public DashboardFinancasResult execute(UUID paroquiaId) {
        Optional<Encontro> corrente = encontroCorrenteResolver.resolver(paroquiaId, LocalDate.now());
        if (corrente.isEmpty()) {
            return new DashboardFinancasResult(FINANCEIRO_VAZIO, null, List.of(), List.of());
        }

        Encontro encontro = corrente.get();
        EscopoTerritorial escopo = new EscopoTerritorial.DeParoquia(paroquiaId);

        return new DashboardFinancasResult(
                obterResumoFinanceiroUseCase.execute(encontro.getId()),
                encontro,
                listarCampanhasUseCase
                        .execute(
                                escopo,
                                encontro.getId(),
                                null,
                                PageRequest.of(0, MAXIMO_LISTAS, Sort.by(Sort.Direction.DESC, "createdAt")))
                        .getContent(),
                listarLancamentosUseCase
                        .execute(
                                escopo,
                                encontro.getId(),
                                null,
                                null,
                                PageRequest.of(0, MAXIMO_LISTAS, Sort.by(Sort.Direction.DESC, "data")))
                        .getContent());
    }
}
