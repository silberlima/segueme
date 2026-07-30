package com.silberlima.segueme.montagem.domain;

/**
 * Evento de domínio publicado ao registrar uma substituição de integrante (RF-032), disparando a
 * notificação automática exigida por RNF-07 — trocar quem serve numa equipe é alteração de vínculo
 * de pessoa e precisa chegar a quem responde por aquela equipe.
 *
 * <p>{@code destinatarioId} é o {@code createdBy} do convite de quem saiu: quem colocou aquela
 * pessoa na equipe. Mesma limitação de identidade documentada em {@code ConviteRecusadoEvent}.
 */
public record SubstituicaoRegistradaEvent(
        String equipeNome,
        int numeroDoEncontro,
        String pessoaSaiNome,
        String pessoaEntraNome,
        String justificativa,
        String destinatarioId) {
}
