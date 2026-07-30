package com.silberlima.segueme.relatorios.domain;

/**
 * Nível territorial gravado junto do {@link Relatorio}. A geração roda depois do commit, num
 * thread sem contexto de segurança, então o escopo do solicitante (RN-04) precisa estar
 * persistido — não dá para resolvê-lo de novo pelas claims na hora de gerar.
 */
public enum NivelDoEscopo {
    NACIONAL,
    DIOCESE,
    SETOR,
    PAROQUIA
}
