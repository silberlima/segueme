package com.silberlima.segueme.acesso.api.dto;

import com.silberlima.segueme.acesso.domain.BuscaResultado;
import java.util.UUID;

public record BuscaResultadoResponse(String tipo, UUID id, String titulo, String subtitulo) {

    public static BuscaResultadoResponse from(BuscaResultado resultado) {
        return new BuscaResultadoResponse(
                resultado.tipo().name(), resultado.id(), resultado.titulo(), resultado.subtitulo());
    }
}
