package com.silberlima.segueme.acesso.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Os 10 perfis de acesso do sistema (RN-ACESSO-02). Cada valor corresponde a uma role
 * de mesmo nome no realm Keycloak {@code segueme} (ver {@code keycloak/segueme-realm.json}).
 */
public enum Perfil {

    NACIONAL,
    DIOCESE,
    SETORIAL,
    PAROQUIA,
    EQUIPE_FICHAS,
    EQUIPE_MONTAGEM,
    EQUIPE_COMANDO,
    EQUIPE_FINANCAS,
    POS_ENCONTRO,
    SEGUIDOR;

    private static final String AUTHORITY_PREFIX = "ROLE_";

    /**
     * Converte uma authority do Spring Security (ex.: {@code ROLE_EQUIPE_MONTAGEM}) no
     * perfil correspondente. Authorities que não representam um dos 10 perfis (ex.: roles
     * técnicas do Keycloak) resultam em {@code Optional.empty()}.
     */
    public static Optional<Perfil> fromAuthority(String authority) {
        if (authority == null || !authority.startsWith(AUTHORITY_PREFIX)) {
            return Optional.empty();
        }
        String name = authority.substring(AUTHORITY_PREFIX.length()).toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(perfil -> perfil.name().equals(name))
                .findFirst();
    }
}
