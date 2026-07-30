package com.silberlima.segueme.notificacao.domain;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Preferências de notificação de um usuário, sempre completas: os três tipos de RN-SISTEMA-01
 * aparecem, mesmo os que ele nunca tocou.
 *
 * <p>É aqui que mora o default "sem registro = ativa" — nem o use case nem a tela precisam saber
 * que a tabela guarda só as escolhas explícitas.
 */
public record PreferenciasDeNotificacao(Map<TipoNotificacao, Boolean> porTipo) {

    private static final boolean ATIVA_POR_PADRAO = true;

    public static PreferenciasDeNotificacao de(List<PreferenciaNotificacao> persistidas) {
        Map<TipoNotificacao, Boolean> porTipo = new EnumMap<>(TipoNotificacao.class);
        Arrays.stream(TipoNotificacao.values()).forEach(tipo -> porTipo.put(tipo, ATIVA_POR_PADRAO));
        persistidas.forEach(preferencia -> porTipo.put(preferencia.getTipo(), preferencia.isAtivo()));
        return new PreferenciasDeNotificacao(porTipo);
    }

    public boolean ativa(TipoNotificacao tipo) {
        return porTipo.getOrDefault(tipo, ATIVA_POR_PADRAO);
    }
}
