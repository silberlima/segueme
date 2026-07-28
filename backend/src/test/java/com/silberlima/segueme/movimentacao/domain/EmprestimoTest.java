package com.silberlima.segueme.movimentacao.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Cobre as transições de estado de {@link Emprestimo} (RF-042 a RF-045, RN-18). */
class EmprestimoTest {

    private final Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
    private final Pessoa diretor = new Pessoa(
            "Diretor", LocalDate.of(1980, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia,
            null, null, null, null, null, null, null);
    private final Encontro encontro =
            new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);
    private final Equipe equipe = new Equipe("Cozinha", 2, 4, null);

    @Test
    void shouldStartSolicitado() {
        Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);

        assertThat(emprestimo.getStatus()).isEqualTo(StatusEmprestimo.SOLICITADO);
    }

    @Test
    void shouldSendSuggestionsFromSolicitado() {
        Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);

        emprestimo.enviarSugestoes();

        assertThat(emprestimo.getStatus()).isEqualTo(StatusEmprestimo.SUGESTOES_ENVIADAS);
    }

    @Test
    void shouldNotSendSuggestionsTwice() {
        Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);
        emprestimo.enviarSugestoes();

        assertThatThrownBy(emprestimo::enviarSugestoes).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldEffectivateAfterSuggestionsSent() {
        Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);
        emprestimo.enviarSugestoes();

        emprestimo.marcarEfetivado();

        assertThat(emprestimo.getStatus()).isEqualTo(StatusEmprestimo.EFETIVADO);
    }

    @Test
    void shouldNotEffectivateBeforeSuggestionsSent() {
        Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);

        assertThatThrownBy(emprestimo::marcarEfetivado).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldRefuseSolicitadoWithJustificativa() {
        Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);

        emprestimo.recusar("Setor sem candidatos disponíveis.");

        assertThat(emprestimo.getStatus()).isEqualTo(StatusEmprestimo.RECUSADO);
        assertThat(emprestimo.getJustificativaRecusa()).isEqualTo("Setor sem candidatos disponíveis.");
    }

    @Test
    void shouldRequireJustificativaToRefuse() {
        Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);

        assertThatThrownBy(() -> emprestimo.recusar("  ")).isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> emprestimo.recusar(null)).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldNotRefuseAfterSuggestionsSent() {
        Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);
        emprestimo.enviarSugestoes();

        assertThatThrownBy(() -> emprestimo.recusar("Motivo qualquer.")).isInstanceOf(BusinessRuleViolationException.class);
    }
}
