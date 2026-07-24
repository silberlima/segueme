package com.silberlima.segueme.inscricoes.api.dto;

import com.silberlima.segueme.inscricoes.application.dto.FunilInscricao;

public record FunilResponse(long preInscritos, long emAnalise, long confirmados, long emReserva, long seguidores) {

    public static FunilResponse from(FunilInscricao funil) {
        return new FunilResponse(
                funil.preInscritos(), funil.emAnalise(), funil.confirmados(), funil.emReserva(), funil.seguidores());
    }
}
