package com.silberlima.segueme.financeiro.api.dto;

import java.math.BigDecimal;

public record ResumoFinanceiroResponse(BigDecimal saldo, BigDecimal totalReceitas, BigDecimal totalDespesas, long campanhasAtivas) {
}
