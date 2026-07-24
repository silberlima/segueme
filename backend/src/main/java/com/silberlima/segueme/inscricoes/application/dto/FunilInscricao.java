package com.silberlima.segueme.inscricoes.application.dto;

/**
 * Contagens por etapa do funil de um Encontro (RF-027). {@code seguidores} é um placeholder
 * zerado até a Fase 6/7 entregar a primeira participação em equipe como Seguidor — mesmo padrão
 * de placeholder documentado já usado em {@code EncontroResponseMapper} (Fase 4).
 */
public record FunilInscricao(long preInscritos, long emAnalise, long confirmados, long emReserva, long seguidores) {
}
