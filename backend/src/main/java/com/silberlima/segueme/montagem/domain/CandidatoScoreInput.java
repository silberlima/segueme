package com.silberlima.segueme.montagem.domain;

/**
 * Entrada plana (sem dependência de JPA/entidade) do {@link SuggestionEngine}, montada pelo Use
 * Case a partir dos repositórios antes de invocar o cálculo puro do score (RF-030).
 *
 * @param participacoesEquipeSemelhante quantas vezes a pessoa já serviu neste mesmo tipo de equipe, em outros Encontros
 * @param totalCompetenciasRequeridas quantas competências o tipo de equipe exige
 * @param competenciasAtendidas quantas dessas competências a pessoa possui
 * @param frequenciaPosEncontroDisponivel se há apuração de pós-encontro para a pessoa (Fase 11); {@code false} significa "sem dado", não 0% de presença
 * @param frequenciaPosEncontroPercentual 0–100, só usado quando {@code frequenciaPosEncontroDisponivel} for {@code true}
 * @param cumprePreRequisito se a pessoa atende o {@code preRequisitoMinEncontros} da equipe (sempre {@code true} quando a equipe não exige pré-requisito)
 */
public record CandidatoScoreInput(
        int participacoesEquipeSemelhante,
        int totalCompetenciasRequeridas,
        int competenciasAtendidas,
        boolean frequenciaPosEncontroDisponivel,
        double frequenciaPosEncontroPercentual,
        boolean cumprePreRequisito) {
}
