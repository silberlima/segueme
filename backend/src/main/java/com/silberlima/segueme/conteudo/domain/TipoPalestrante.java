package com.silberlima.segueme.conteudo.domain;

/**
 * Os 3 tipos de palestrante (RN-CONTEUDO-02): apenas {@link #SEGUIDOR} referencia um cadastro de
 * {@link com.silberlima.segueme.pessoas.domain.Pessoa} — {@link #EXTERNO} e {@link #CLERO} nunca
 * geram cadastro de pessoa no sistema nacional.
 */
public enum TipoPalestrante {
    EXTERNO,
    SEGUIDOR,
    CLERO
}
