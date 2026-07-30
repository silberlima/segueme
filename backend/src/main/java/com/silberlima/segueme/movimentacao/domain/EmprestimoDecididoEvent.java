package com.silberlima.segueme.movimentacao.domain;

/**
 * Evento de domínio publicado quando o Setorial responde a um Empréstimo (RF-044/046) — com
 * candidatos ou com recusa —, disparando a notificação à Equipe Montagem exigida por RNF-07. É o
 * lado de volta do fluxo bidirecional de RN-MOVIMENTO-04: sem ele, a Montagem só saberia da
 * resposta olhando a tela de novo.
 *
 * <p>{@code destinatarioId} é o {@code createdBy} do Empréstimo — a Montagem que solicitou.
 */
public record EmprestimoDecididoEvent(
        String equipeNome,
        int numeroDoEncontro,
        int candidatosEnviados,
        boolean recusado,
        String justificativaRecusa,
        String destinatarioId) {
}
