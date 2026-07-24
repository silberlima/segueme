package com.silberlima.segueme.inscricoes.application.dto;

import com.silberlima.segueme.inscricoes.domain.CadastroReserva;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;

/**
 * Resultado de {@code PreInscreverUseCase}: exatamente um dos dois campos é preenchido,
 * dependendo de a vaga do Encontro estar disponível ou esgotada (RN-INSCRICAO-03).
 */
public record ResultadoPreInscricao(PreInscricao preInscricao, CadastroReserva cadastroReserva) {

    public boolean entrouEmReserva() {
        return cadastroReserva != null;
    }
}
