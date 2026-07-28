package com.silberlima.segueme.movimentacao.domain;

/** Status de um {@link Emprestimo} (`transferencias-e-emprestimos.md` 2.2, RN-18). */
public enum StatusEmprestimo {
    SOLICITADO,
    SUGESTOES_ENVIADAS,
    EFETIVADO,
    RECUSADO
}
