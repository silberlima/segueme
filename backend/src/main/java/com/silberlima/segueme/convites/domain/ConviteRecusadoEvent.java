package com.silberlima.segueme.convites.domain;

import java.util.UUID;

/**
 * Evento de domínio publicado quando um convite é recusado (RF-037), disparando a notificação
 * imediata prevista em RF-038/RN-12. {@code destinatarioId} é o {@code createdBy} do
 * {@link com.silberlima.segueme.montagem.domain.IntegranteEquipe} recusado, ou seja, quem emitiu
 * aquele convite — nesta fase, sempre alguém da Equipe Comando (ver
 * {@code EmitirConviteUseCase}), já que não existe diretório de usuários por role para resolver
 * "a Equipe Montagem" literalmente. É o destinatário mais próximo do que RN-12 pede que o sistema
 * consegue resolver hoje sem essa infraestrutura — pendência documentada em
 * {@code docs/pendencias-gerais.md}.
 */
public record ConviteRecusadoEvent(
        UUID integranteEquipeId,
        UUID encontroId,
        String equipeNome,
        String pessoaNome,
        String justificativa,
        String destinatarioId) {
}
