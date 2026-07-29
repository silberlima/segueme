package com.silberlima.segueme.posencontro.domain;

/**
 * Faixas de prioridade para ser convidado a servir nas próximas equipes (RN-POS-02/03). A régua
 * numérica que separa as faixas não está especificada no protótipo (pendência #14 de
 * {@code pendencias-gerais.md}) e por isso é parametrizável — ver {@link ReguaPrioridade}.
 */
public enum FaixaPrioridade {
    ALTA,
    MEDIA,
    BAIXA
}
