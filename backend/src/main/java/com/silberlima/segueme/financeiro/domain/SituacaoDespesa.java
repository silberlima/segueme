package com.silberlima.segueme.financeiro.domain;

/** Ciclo de situação de uma despesa (`financeiro.md` 2.3, RN-FINANCEIRO-02). Não se aplica a receitas. */
public enum SituacaoDespesa {
    PENDENTE,
    APROVADA,
    PAGA
}
