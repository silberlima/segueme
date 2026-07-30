package com.silberlima.segueme.notificacao.api.dto;

import com.silberlima.segueme.notificacao.domain.PreferenciasDeNotificacao;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import java.util.Arrays;
import java.util.List;

public record PreferenciaNotificacaoResponse(String tipo, boolean ativo) {

    public static List<PreferenciaNotificacaoResponse> from(PreferenciasDeNotificacao preferencias) {
        return Arrays.stream(TipoNotificacao.values())
                .map(tipo -> new PreferenciaNotificacaoResponse(tipo.name(), preferencias.ativa(tipo)))
                .toList();
    }
}
