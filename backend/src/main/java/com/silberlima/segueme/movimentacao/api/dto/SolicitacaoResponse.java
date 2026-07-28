package com.silberlima.segueme.movimentacao.api.dto;

import com.silberlima.segueme.movimentacao.application.dto.TipoSolicitacao;
import java.time.Instant;
import java.util.UUID;

/** Item do inbox único do Setorial (RF-046): uma Transferência ou um Empréstimo pendente. */
public record SolicitacaoResponse(
        UUID id,
        TipoSolicitacao tipo,
        Instant createdAt,
        String descricao,
        String pessoaNome,
        String paroquiaOrigemNome,
        String paroquiaDestinoNome,
        String equipeNome,
        Integer encontroNumero,
        String competenciaNome) {
}
