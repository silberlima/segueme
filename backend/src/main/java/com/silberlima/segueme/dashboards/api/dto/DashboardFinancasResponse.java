package com.silberlima.segueme.dashboards.api.dto;

import com.silberlima.segueme.financeiro.api.dto.CampanhaResponse;
import com.silberlima.segueme.financeiro.api.dto.LancamentoFinanceiroResponse;
import com.silberlima.segueme.financeiro.api.dto.ResumoFinanceiroResponse;
import java.util.List;

/** Painel Financeiro (`dash_financas`, RF-053): saldo do Encontro corrente, campanhas e lançamentos. */
public record DashboardFinancasResponse(
        ResumoFinanceiroResponse resumo,
        EncontroDoPainelResponse encontroCorrente,
        List<CampanhaResponse> campanhas,
        List<LancamentoFinanceiroResponse> lancamentosRecentes) {
}
