package com.silberlima.segueme.montagem.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Cobre {@link SuggestionEngine} (RF-030, RN-09/RN-MONTAGEM-03) — domain service puro, sem mocks. */
class SuggestionEngineTest {

    private final SuggestionEngine suggestionEngine = new SuggestionEngine();

    @Test
    void shouldReturnMaxScoreWhenAllFactorsAreFullyMet() {
        MatchScore score = suggestionEngine.calcular(new CandidatoScoreInput(5, 2, 2, false, 0, true));

        assertThat(score.historicoEquipesSemelhantes()).isEqualTo(100);
        assertThat(score.competencias()).isEqualTo(100);
        assertThat(score.frequenciaPosEncontro()).isZero();
        assertThat(score.preRequisito()).isEqualTo(100);
        // 100*0.35 + 100*0.30 + 0*0.20 + 100*0.15 = 80
        assertThat(score.total()).isEqualTo(80);
    }

    @Test
    void shouldReturnZeroScoreWhenNoFactorIsMet() {
        MatchScore score = suggestionEngine.calcular(new CandidatoScoreInput(0, 3, 0, false, 0, false));

        assertThat(score.historicoEquipesSemelhantes()).isZero();
        assertThat(score.competencias()).isZero();
        assertThat(score.preRequisito()).isZero();
        assertThat(score.total()).isZero();
    }

    @Test
    void shouldCapHistoricoScoreAtOneHundredAfterTwoParticipations() {
        MatchScore comDuasParticipacoes = suggestionEngine.calcular(new CandidatoScoreInput(2, 0, 0, false, 0, false));
        MatchScore comCincoParticipacoes = suggestionEngine.calcular(new CandidatoScoreInput(5, 0, 0, false, 0, false));

        assertThat(comDuasParticipacoes.historicoEquipesSemelhantes()).isEqualTo(100);
        assertThat(comCincoParticipacoes.historicoEquipesSemelhantes()).isEqualTo(100);
    }

    @Test
    void shouldReturnFullCompetenciasScoreWhenTeamRequiresNone() {
        MatchScore score = suggestionEngine.calcular(new CandidatoScoreInput(0, 0, 0, false, 0, false));

        assertThat(score.competencias()).isEqualTo(100);
    }

    @Test
    void shouldProrateCompetenciasScoreByProportionAttended() {
        MatchScore score = suggestionEngine.calcular(new CandidatoScoreInput(0, 4, 1, false, 0, false));

        assertThat(score.competencias()).isEqualTo(25);
    }

    @Test
    void shouldIgnoreFrequenciaPosEncontroWhenThereIsNoData() {
        MatchScore comDadoAlto = suggestionEngine.calcular(new CandidatoScoreInput(0, 0, 0, false, 100, false));

        assertThat(comDadoAlto.frequenciaPosEncontro()).isZero();
    }

    @Test
    void shouldUseFrequenciaPosEncontroPercentualWhenAvailable() {
        MatchScore score = suggestionEngine.calcular(new CandidatoScoreInput(0, 0, 0, true, 60, false));

        assertThat(score.frequenciaPosEncontro()).isEqualTo(60);
    }

    @Test
    void shouldExposeScoreVersion() {
        MatchScore score = suggestionEngine.calcular(new CandidatoScoreInput(0, 0, 0, false, 0, false));

        assertThat(score.versao()).isEqualTo(SuggestionEngine.VERSAO);
    }
}
