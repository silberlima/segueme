package com.silberlima.segueme.posencontro.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Chamada de um encontro pós enviada em lote (UC — Registrar presença): a tela marca a lista inteira
 * e salva de uma vez, em vez de uma requisição por jovem.
 */
public record RegistrarPresencasRequest(@NotEmpty List<@Valid PresencaItem> presencas) {

    public record PresencaItem(@NotNull UUID pessoaId, @NotNull Boolean presente) {
    }
}
