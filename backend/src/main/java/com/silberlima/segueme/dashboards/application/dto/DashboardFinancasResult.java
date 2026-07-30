package com.silberlima.segueme.dashboards.application.dto;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.financeiro.application.dto.ResumoFinanceiro;
import com.silberlima.segueme.financeiro.domain.Campanha;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiro;
import java.util.List;

/**
 * Painel Financeiro (`dash_financas`, RF-053): saldo, receitas, despesas e campanhas ativas do
 * Encontro corrente, com as campanhas em progresso e os últimos lançamentos. Os KPIs reaproveitam
 * {@code ObterResumoFinanceiroUseCase} — o mesmo saldo calculado da tela de financeiro
 * (RN-FINANCEIRO-04), nunca um segundo cálculo paralelo.
 */
public record DashboardFinancasResult(
        ResumoFinanceiro resumo,
        Encontro encontroCorrente,
        List<Campanha> campanhas,
        List<LancamentoFinanceiro> lancamentosRecentes) {
}
