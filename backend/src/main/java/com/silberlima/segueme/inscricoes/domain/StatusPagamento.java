package com.silberlima.segueme.inscricoes.domain;

/**
 * Situação de pagamento de uma Ficha de Inscrição, exibida na coluna de pagamento de
 * `screen_participantes` (`inscricoes.md` 2.4). Placeholder mínimo até a Fase 9 (Financeiro)
 * definir valores, formas e prazos (pendência #13 de `pendencias-gerais.md`).
 */
public enum StatusPagamento {
    PENDENTE,
    PAGO
}
