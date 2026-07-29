package com.silberlima.segueme.conteudo.application.dto;

import com.silberlima.segueme.conteudo.domain.Palestra;
import com.silberlima.segueme.conteudo.domain.Palestrante;

/** Um tema do catálogo nacional ({@link Palestra}) combinado com o palestrante designado (se houver) num Encontro específico. */
public record PalestraDoEncontro(Palestra palestra, Palestrante palestrante) {
}
