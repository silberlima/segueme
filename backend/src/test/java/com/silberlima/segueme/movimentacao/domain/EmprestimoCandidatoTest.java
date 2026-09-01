package com.silberlima.segueme.movimentacao.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Cobre a efetivação de um {@link EmprestimoCandidato} sugerido pelo Setorial (RF-045). */
class EmprestimoCandidatoTest {

    private final Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
    private final Pessoa diretor = new Pessoa(
            "Diretor",
            PessoasDeTeste.cpf(),
            LocalDate.of(1980, 1, 1),
            "(11) 90000-0000",
            null,
            EstadoCivil.SOLTEIRO,
            paroquia,
            PessoasDeTeste.endereco(),
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    private final Encontro encontro =
            new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);
    private final Equipe equipe = new Equipe("Cozinha", 2, 4, null);
    private final Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);
    private final Pessoa candidato = new Pessoa(
            "Candidato",
            PessoasDeTeste.cpf(),
            LocalDate.of(2000, 1, 1),
            "(11) 90000-0000",
            null,
            EstadoCivil.SOLTEIRO,
            paroquia,
            PessoasDeTeste.endereco(),
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    @Test
    void shouldStartSugerido() {
        EmprestimoCandidato emprestimoCandidato = new EmprestimoCandidato(emprestimo, candidato);

        assertThat(emprestimoCandidato.getStatus()).isEqualTo(StatusCandidatoEmprestimo.SUGERIDO);
        assertThat(emprestimoCandidato.getIntegranteEquipe()).isNull();
    }

    @Test
    void shouldEffectivateSugeridoCandidate() {
        EmprestimoCandidato emprestimoCandidato = new EmprestimoCandidato(emprestimo, candidato);
        IntegranteEquipe integrante = new IntegranteEquipe(encontro, equipe, candidato, PapelIntegrante.INTEGRANTE);

        emprestimoCandidato.efetivar(integrante);

        assertThat(emprestimoCandidato.getStatus()).isEqualTo(StatusCandidatoEmprestimo.EFETIVADO);
        assertThat(emprestimoCandidato.getIntegranteEquipe()).isEqualTo(integrante);
    }

    @Test
    void shouldNotEffectivateTwice() {
        EmprestimoCandidato emprestimoCandidato = new EmprestimoCandidato(emprestimo, candidato);
        IntegranteEquipe integrante = new IntegranteEquipe(encontro, equipe, candidato, PapelIntegrante.INTEGRANTE);
        emprestimoCandidato.efetivar(integrante);

        assertThatThrownBy(() -> emprestimoCandidato.efetivar(integrante)).isInstanceOf(BusinessRuleViolationException.class);
    }
}
