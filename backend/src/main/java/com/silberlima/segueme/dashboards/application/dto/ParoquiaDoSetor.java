package com.silberlima.segueme.dashboards.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Linha da lista "Paróquias do setor" do Painel do Setor (`dashboards-por-perfil.md` 3.3):
 * contagem de seguidores e data do próximo Encontro. {@code proximoEncontro} nulo = a paróquia não
 * tem Encontro ativo em aberto.
 */
public record ParoquiaDoSetor(UUID paroquiaId, String nome, long seguidores, LocalDate proximoEncontro) {
}
