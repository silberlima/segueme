package com.silberlima.segueme.posencontro.application.dto;

import java.util.UUID;

/** Uma marcação de presença a registrar em lote (UC — Registrar presença). */
public record PresencaRegistro(UUID pessoaId, boolean presente) {
}
