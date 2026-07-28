package com.silberlima.segueme.convites.api.dto;

import jakarta.validation.constraints.NotBlank;

/** Recusa de convite (RF-037): justificativa é obrigatória (RN-CONVITE-02). */
public record RecusarConviteRequest(@NotBlank String justificativa) {
}
