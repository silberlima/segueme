package com.silberlima.segueme.financeiro.application.dto;

import java.math.BigDecimal;

/** Saúde financeira consolidada de um Encontro (RN-FINANCEIRO-04): calculado, não persistido. */
public record ResumoFinanceiro(BigDecimal saldo, BigDecimal totalReceitas, BigDecimal totalDespesas, long campanhasAtivas) {
}
