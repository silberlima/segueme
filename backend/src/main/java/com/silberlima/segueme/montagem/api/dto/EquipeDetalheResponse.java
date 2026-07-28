package com.silberlima.segueme.montagem.api.dto;

import java.util.List;
import java.util.UUID;

/** Detalhe de uma equipe de serviço de um Encontro: composição, vagas e pré-requisito (RF-028). */
public record EquipeDetalheResponse(
        UUID id,
        String nome,
        int minimo,
        int maximo,
        Integer preRequisitoMinEncontros,
        List<String> competenciasRequeridas,
        long preenchidos,
        long vagasAbertas,
        boolean completa,
        List<IntegranteResponse> integrantes) {
}
