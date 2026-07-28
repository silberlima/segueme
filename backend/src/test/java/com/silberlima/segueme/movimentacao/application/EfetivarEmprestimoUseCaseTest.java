package com.silberlima.segueme.movimentacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.application.AdicionarIntegranteUseCase;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoCandidato;
import com.silberlima.segueme.movimentacao.domain.EmprestimoCandidatoRepository;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import com.silberlima.segueme.movimentacao.domain.StatusCandidatoEmprestimo;
import com.silberlima.segueme.movimentacao.domain.StatusEmprestimo;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EfetivarEmprestimoUseCaseTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private EmprestimoCandidatoRepository emprestimoCandidatoRepository;

    @Mock
    private AdicionarIntegranteUseCase adicionarIntegranteUseCase;

    private final Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
    private final Pessoa diretor = new Pessoa(
            "Diretor", LocalDate.of(1980, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia,
            null, null, null, null, null, null, null);
    private final Encontro encontro =
            new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);
    private final Equipe equipe = new Equipe("Cozinha", 2, 4, null);

    @Test
    void shouldAddAllSuggestedCandidatesViaEmprestimoAprovadoAndMarkEffectivated() {
        UUID emprestimoId = UUID.randomUUID();
        ReflectionTestUtils.setField(encontro, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(equipe, "id", UUID.randomUUID());
        Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);
        emprestimo.enviarSugestoes();
        Pessoa candidatoPessoa = new Pessoa(
                "Candidato", LocalDate.of(2000, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia,
                null, null, null, null, null, null, null);
        ReflectionTestUtils.setField(candidatoPessoa, "id", UUID.randomUUID());
        EmprestimoCandidato candidato = new EmprestimoCandidato(emprestimo, candidatoPessoa);
        IntegranteEquipe integrante = new IntegranteEquipe(encontro, equipe, candidatoPessoa, PapelIntegrante.INTEGRANTE);
        when(emprestimoRepository.findByIdAndDeletedFalse(emprestimoId)).thenReturn(Optional.of(emprestimo));
        when(emprestimoCandidatoRepository.findByEmprestimo_IdAndStatus(emprestimoId, StatusCandidatoEmprestimo.SUGERIDO))
                .thenReturn(List.of(candidato));
        when(adicionarIntegranteUseCase.execute(
                        encontro.getId(), equipe.getId(), candidatoPessoa.getId(), PapelIntegrante.INTEGRANTE, true))
                .thenReturn(integrante);

        Emprestimo resultado = new EfetivarEmprestimoUseCase(emprestimoRepository, emprestimoCandidatoRepository, adicionarIntegranteUseCase)
                .execute(emprestimoId);

        assertThat(resultado.getStatus()).isEqualTo(StatusEmprestimo.EFETIVADO);
        assertThat(candidato.getStatus()).isEqualTo(StatusCandidatoEmprestimo.EFETIVADO);
        assertThat(candidato.getIntegranteEquipe()).isEqualTo(integrante);
    }

    @Test
    void shouldThrowWhenNoCandidatesSuggested() {
        UUID emprestimoId = UUID.randomUUID();
        Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);
        emprestimo.enviarSugestoes();
        when(emprestimoRepository.findByIdAndDeletedFalse(emprestimoId)).thenReturn(Optional.of(emprestimo));
        when(emprestimoCandidatoRepository.findByEmprestimo_IdAndStatus(emprestimoId, StatusCandidatoEmprestimo.SUGERIDO))
                .thenReturn(List.of());

        assertThatThrownBy(() -> new EfetivarEmprestimoUseCase(emprestimoRepository, emprestimoCandidatoRepository, adicionarIntegranteUseCase)
                        .execute(emprestimoId))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenEmprestimoNotFound() {
        UUID emprestimoId = UUID.randomUUID();
        when(emprestimoRepository.findByIdAndDeletedFalse(emprestimoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new EfetivarEmprestimoUseCase(emprestimoRepository, emprestimoCandidatoRepository, adicionarIntegranteUseCase)
                        .execute(emprestimoId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
