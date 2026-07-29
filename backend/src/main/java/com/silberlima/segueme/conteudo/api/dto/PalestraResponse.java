package com.silberlima.segueme.conteudo.api.dto;

import java.util.UUID;

/** Um tema oficial do encontro, com o palestrante designado (campos {@code palestranteX} nulos quando ainda não há designação). */
public record PalestraResponse(
        UUID palestraId, int numero, String tema, UUID palestranteId, String palestranteNome, String palestranteTipo) {
}
