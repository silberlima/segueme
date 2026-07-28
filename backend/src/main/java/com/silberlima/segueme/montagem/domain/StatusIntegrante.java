package com.silberlima.segueme.montagem.domain;

/**
 * Status de um {@link IntegranteEquipe} (`montagem-de-equipes.md` 2.1). Todo integrante nasce
 * {@code PENDENTE}; a transição para {@code ACEITO}/{@code RECUSADO} acontece via
 * {@link IntegranteEquipe#aceitar()}/{@link IntegranteEquipe#recusar(String)} (ciclo de Convite da
 * Fase 7, RN-CONVITE-01/02) ou, quando é a Equipe Montagem quem encerra o vínculo, via
 * {@link IntegranteEquipe#marcarRecusado()} chamado por {@link Substituicao}, que preserva o
 * registro em vez de removê-lo (RN-MONTAGEM-06).
 */
public enum StatusIntegrante {
    PENDENTE,
    ACEITO,
    RECUSADO
}
