package com.silberlima.segueme.acesso.domain;

import java.util.UUID;

public record BuscaResultado(BuscaTipo tipo, UUID id, String titulo, String subtitulo) {
}
