package com.silberlima.segueme.montagem.domain;

/**
 * Status de um {@link IntegranteEquipe} (`montagem-de-equipes.md` 2.1). A transição
 * PENDENTE → ACEITO/RECUSADO pertence ao ciclo de Convite da Fase 7 (RN-CONVITE-01,
 * ainda não implementado); nesta fase todo integrante nasce {@code PENDENTE} e só se torna
 * {@code RECUSADO} via {@link Substituicao}, que preserva o registro em vez de removê-lo
 * (RN-MONTAGEM-06).
 */
public enum StatusIntegrante {
    PENDENTE,
    ACEITO,
    RECUSADO
}
