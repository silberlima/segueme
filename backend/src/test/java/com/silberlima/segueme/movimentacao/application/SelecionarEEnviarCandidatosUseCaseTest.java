package com.silberlima.segueme.movimentacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoCandidato;
import com.silberlima.segueme.movimentacao.domain.EmprestimoCandidatoRepository;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import com.silberlima.segueme.movimentacao.domain.StatusEmprestimo;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
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
class SelecionarEEnviarCandidatosUseCaseTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private EmprestimoCandidatoRepository emprestimoCandidatoRepository;

    private final Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
    private final Pessoa diretor = new Pessoa(
            "Diretor", LocalDate.of(1980, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia,
            null, null, null, null, null, null, null);
    private final Encontro encontro =
            new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);
    private final Equipe equipe = new Equipe("Cozinha", 2, 4, null);

    @Test
    void shouldCreateCandidatesAndSendSuggestions() {
        UUID emprestimoId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);
        Pessoa candidato = umaPessoa(pessoaId);
        when(emprestimoRepository.findByIdAndDeletedFalse(emprestimoId)).thenReturn(Optional.of(emprestimo));
        when(emprestimoCandidatoRepository.existsByEmprestimo_IdAndPessoa_Id(emprestimoId, pessoaId)).thenReturn(false);
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.of(candidato));

        Emprestimo resultado = new SelecionarEEnviarCandidatosUseCase(emprestimoRepository, pessoaRepository, emprestimoCandidatoRepository)
                .execute(emprestimoId, List.of(pessoaId));

        assertThat(resultado.getStatus()).isEqualTo(StatusEmprestimo.SUGESTOES_ENVIADAS);
        verify(emprestimoCandidatoRepository).save(org.mockito.ArgumentMatchers.any(EmprestimoCandidato.class));
    }

    @Test
    void shouldSkipAlreadyExistingCandidate() {
        UUID emprestimoId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);
        when(emprestimoRepository.findByIdAndDeletedFalse(emprestimoId)).thenReturn(Optional.of(emprestimo));
        when(emprestimoCandidatoRepository.existsByEmprestimo_IdAndPessoa_Id(emprestimoId, pessoaId)).thenReturn(true);

        new SelecionarEEnviarCandidatosUseCase(emprestimoRepository, pessoaRepository, emprestimoCandidatoRepository)
                .execute(emprestimoId, List.of(pessoaId));

        verify(emprestimoCandidatoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(pessoaRepository, times(0)).findByIdAndDeletedFalse(pessoaId);
    }

    @Test
    void shouldThrowWhenEmprestimoNotFound() {
        UUID emprestimoId = UUID.randomUUID();
        when(emprestimoRepository.findByIdAndDeletedFalse(emprestimoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new SelecionarEEnviarCandidatosUseCase(emprestimoRepository, pessoaRepository, emprestimoCandidatoRepository)
                        .execute(emprestimoId, List.of()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Pessoa umaPessoa(UUID id) {
        Pessoa pessoa = new Pessoa(
                "Candidato", LocalDate.of(2000, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia,
                null, null, null, null, null, null, null);
        ReflectionTestUtils.setField(pessoa, "id", id);
        return pessoa;
    }
}
