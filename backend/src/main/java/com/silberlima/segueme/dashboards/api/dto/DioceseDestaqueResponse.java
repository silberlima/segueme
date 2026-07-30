package com.silberlima.segueme.dashboards.api.dto;

import java.util.UUID;

/** Linha do ranking "Dioceses em destaque" do Painel Nacional. */
public record DioceseDestaqueResponse(
        UUID dioceseId, String nome, long paroquias, long seguidores, long encontrosNoAno) {
}
