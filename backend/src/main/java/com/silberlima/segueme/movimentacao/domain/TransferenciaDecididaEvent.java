package com.silberlima.segueme.movimentacao.domain;

/**
 * Evento de domínio publicado quando o Setorial decide uma Transferência (RF-041), disparando a
 * notificação automática à parte responsável exigida por RNF-07: mudança de vínculo de pessoa é
 * auditável e não pode depender de alguém reabrir a tela para descobrir o desfecho.
 *
 * <p>{@code destinatarioId} é o {@code createdBy} da Transferência — quem solicitou. Mesma
 * limitação de identidade documentada em {@code ConviteRecusadoEvent}: sem diretório de usuários
 * por role, notifica-se quem criou o registro, não "a paróquia de origem" literalmente.
 */
public record TransferenciaDecididaEvent(
        String pessoaNome,
        String paroquiaOrigemNome,
        String paroquiaDestinoNome,
        boolean aprovada,
        String justificativaRecusa,
        String destinatarioId) {
}
