package com.silberlima.segueme.montagem.domain;

/**
 * Domain service puro (sem dependência de framework — nenhum import Spring/JPA) que calcula o
 * score de match 0–100 de um candidato a uma vaga de equipe (RF-030, RN-09/RN-MONTAGEM-03). Os
 * pesos 35/30/20/15% são fixos por regra de negócio; a fórmula interna de cada fator é melhor
 * esforço documentado (pendência #6 de {@code pendencias-gerais.md}), versionada em
 * {@link #VERSAO} para permitir recalibração sem quebrar o contrato do endpoint que expõe o
 * score.
 *
 * <p>O fator de frequência no pós-encontro nasceu zerado na Fase 6 por não haver dado
 * ({@code plano-implementacao.md} seção 19) e passou a usar a apuração real com a entrega da
 * Fase 11 — {@code frequenciaPosEncontroDisponivel} continua distinguindo "0% de presença" de
 * "nenhum encontro pós registrado para esta pessoa", que não pode virar 0 pontos sob pena de
 * penalizar candidatos de paróquias que ainda não fazem acompanhamento.
 */
public class SuggestionEngine {

    /** Identificador da fórmula atual, exposto na resposta da API para permitir recalibração futura sem quebra de contrato. */
    public static final String VERSAO = "v2-frequencia-posencontro";

    private static final double PESO_HISTORICO = 0.35;
    private static final double PESO_COMPETENCIAS = 0.30;
    private static final double PESO_FREQUENCIA_POS_ENCONTRO = 0.20;
    private static final double PESO_PRE_REQUISITO = 0.15;

    /** Cada participação anterior no mesmo tipo de equipe soma 50 pontos, até o teto de 100. */
    private static final int PONTOS_POR_PARTICIPACAO_SEMELHANTE = 50;

    public MatchScore calcular(CandidatoScoreInput entrada) {
        int historico = calcularHistorico(entrada.participacoesEquipeSemelhante());
        int competencias = calcularCompetencias(entrada.totalCompetenciasRequeridas(), entrada.competenciasAtendidas());
        int frequencia = calcularFrequenciaPosEncontro(entrada);
        int preRequisito = entrada.cumprePreRequisito() ? 100 : 0;

        int total = (int) Math.round(
                historico * PESO_HISTORICO
                        + competencias * PESO_COMPETENCIAS
                        + frequencia * PESO_FREQUENCIA_POS_ENCONTRO
                        + preRequisito * PESO_PRE_REQUISITO);

        return new MatchScore(total, historico, competencias, frequencia, preRequisito, VERSAO);
    }

    private int calcularHistorico(int participacoesEquipeSemelhante) {
        return Math.min(100, participacoesEquipeSemelhante * PONTOS_POR_PARTICIPACAO_SEMELHANTE);
    }

    private int calcularCompetencias(int totalRequeridas, int atendidas) {
        if (totalRequeridas <= 0) {
            return 100;
        }
        return (int) Math.round((atendidas * 100.0) / totalRequeridas);
    }

    /** Sem dado de pós-encontro para a pessoa, o fator não pontua — ver a nota de classe sobre a Fase 11. */
    private int calcularFrequenciaPosEncontro(CandidatoScoreInput entrada) {
        if (!entrada.frequenciaPosEncontroDisponivel()) {
            return 0;
        }
        double percentual = Math.max(0, Math.min(100, entrada.frequenciaPosEncontroPercentual()));
        return (int) Math.round(percentual);
    }
}
