package com.silberlima.segueme.relatorios.domain;

/**
 * Ciclo de vida de um {@link Relatorio}. Como a geração é assíncrona (skills/10: operação demorada
 * não bloqueia a request), a solicitação nasce {@code PENDENTE} e a tela acompanha até o desfecho.
 */
public enum StatusRelatorio {
    PENDENTE,
    CONCLUIDO,
    FALHOU
}
