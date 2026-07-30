package com.silberlima.segueme.notificacao.api.dto;

import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Toggles da seção "Notificações" de Configurações (RF-057). A tela envia o conjunto que está
 * exibindo, não um toggle por requisição — assim salvar é uma ação só, como no protótipo.
 */
public record AtualizarPreferenciasRequest(@NotEmpty List<@Valid Preferencia> preferencias) {

    public Map<TipoNotificacao, Boolean> paraEscolhas() {
        Map<TipoNotificacao, Boolean> escolhas = new LinkedHashMap<>();
        preferencias.forEach(preferencia -> escolhas.put(preferencia.tipo(), preferencia.ativo()));
        return escolhas;
    }

    public record Preferencia(@NotNull TipoNotificacao tipo, @NotNull Boolean ativo) {
    }
}
