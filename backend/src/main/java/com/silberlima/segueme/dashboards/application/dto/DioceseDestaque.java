package com.silberlima.segueme.dashboards.application.dto;

import java.util.UUID;

/** Linha do ranking "Dioceses em destaque" do Painel Nacional (`dashboards-por-perfil.md` 3.1). */
public record DioceseDestaque(UUID dioceseId, String nome, long paroquias, long seguidores, long encontrosNoAno) {
}
