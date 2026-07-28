package com.silberlima.segueme.movimentacao.application.dto;

import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import java.time.Instant;

/**
 * Item de orquestração do inbox de Solicitações — não é uma entidade própria
 * ({@code plano-implementacao.md}: Solicitação é "agregação/view", não necessariamente tabela).
 * Exatamente um dos dois campos ({@code transferencia}/{@code emprestimo}) é não-nulo, conforme
 * {@code tipo}.
 */
public record SolicitacaoItem(TipoSolicitacao tipo, Transferencia transferencia, Emprestimo emprestimo, Instant createdAt) {

    public static SolicitacaoItem de(Transferencia transferencia) {
        return new SolicitacaoItem(TipoSolicitacao.TRANSFERENCIA, transferencia, null, transferencia.getCreatedAt());
    }

    public static SolicitacaoItem de(Emprestimo emprestimo) {
        return new SolicitacaoItem(TipoSolicitacao.EMPRESTIMO, null, emprestimo, emprestimo.getCreatedAt());
    }
}
