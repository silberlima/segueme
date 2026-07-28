package com.silberlima.segueme.montagem.api.dto;

import java.util.UUID;

/** Linha da lista de equipes de serviço de um Encontro (RF-028). */
public record EquipeResumoResponse(
        UUID id, String nome, int minimo, int maximo, long preenchidos, long vagasAbertas, boolean completa) {
}
