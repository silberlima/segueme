package com.silberlima.segueme.pessoas.domain;

/**
 * Situação de uma Pessoa no funil do movimento (RN-PESSOA-05): evolui
 * Pré-inscrito → (ficha, Fase 5) → Seguimista → (1ª equipe, Fase 6/7) → Seguidor.
 * {@code INSCRITO} existe no enunciado da Fase 3 do plano mas seu gatilho de transição
 * pertence à Fase 5 (Inscrições) — modelado aqui só como valor possível, sem transição própria.
 */
public enum SituacaoPessoa {
    PRE_INSCRITO,
    INSCRITO,
    SEGUIMISTA,
    SEGUIDOR
}
