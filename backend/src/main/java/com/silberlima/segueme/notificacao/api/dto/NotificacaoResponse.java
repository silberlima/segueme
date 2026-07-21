package com.silberlima.segueme.notificacao.api.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificacaoResponse(
        UUID id, String tipo, String titulo, String mensagem, boolean lida, Instant createdAt) {
}
